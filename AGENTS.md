# uplift project notes

## HTTP keep-alive for uplift-synchttp -- COMPLETE

### Status

Latest benchmark (2026-11-06, after review sections A, B and C, macOS ARM, loopback):

| Benchmark | Score (ops/s) | Error |
|---|---|---|
| Netty | 17,990 | +/- 1,462 |
| upliftSynchttp | 17,102 | +/- 946 |

The gap to Netty in `HttpBenchmark` is now about 1.05x. The earlier note that the remaining
~1.3x gap was fundamental did not hold: most of it was per-request allocation (a direct
ByteBuffer per header lookup caused heavy GC) and two writes per response.
`HttpBenchmark` is dominated by the JDK HttpClient; use `RawSocketBenchmark` for server cost.

### What was done

1. **Keep-alive loop** -- `Server.Processor.process()` returns `boolean`,
   `DefaultServer.processChannel()` loops while processor returns `true`
2. **EOF handling** -- `HttpReqReader.read()` returns `null` on EOF
3. **Connection: close** -- `HttpCallbackProcessor` checks header, returns `false`
4. **Channel lifetime** -- `HttpResWriter` no longer closes the output channel;
   `DefaultServer` closes the socket after the loop exits
5. **Body-channel fix** -- `HttpReqReader.body()` returns empty channel for
   bodyless requests instead of raw socket
6. **Segments pool** -- MemorySegment slices from pre-allocated arena for both
   request reading (`reqSegments`) and response buffering (`resSegments`);
   released via `HttpReq.close()` and `resSegments.release()` respectively
7. **body(byte[]) shortcut** -- direct `ByteBuffer.wrap(bytes)` write,
   bypasses channel wrapping and direct buffer allocation
8. **Buffered headers** -- status line + headers buffered into single write
   (neutral for small responses)
9. **String formatting cleanup** (neutral)
10. **CompletableFuture removal** (neutral with keep-alive)
11. **Processor extends RuntimeCloseable** -- `DefaultServer.close()` calls
    `processor.close()` for Segments pool cleanup

### Benchmark progression

| Stage | uplift ops/s | Error | Gap to Netty |
|---|---|---|---|
| Baseline (no keep-alive) | 8,151 | - | 2.3x |
| Keep-alive (no pool) | 13,518 | +/- 4,674 | 1.4x |
| Keep-alive + pool + body fix | 14,420 | +/- 94 | 1.3x |
| + body(byte[]) shortcut | 15,027 | +/- 221 | 1.2x |
| + all remaining optimizations | 14,631 | +/- 209 | 1.3x |
| + resSegments pool fix | 15,301 | +/- 412 | 1.3x |
| 2026-11-06, after A (correctness) fixes | 14,549 | +/- 577 | 1.24x |
| + B (protocol) fixes | 14,254 | +/- 1,281 | 1.25x |
| + C (allocation-free headers, single write, TCP_NODELAY) | 17,102 | +/- 946 | 1.05x |

`RawSocketBenchmark` (added 2026-11-06) measures the server without the JDK HttpClient:
one keep-alive connection, browser-like headers, handler reads headers and query parameters.
Run: `java --enable-preview --add-modules jdk.incubator.vector -jar target/*-jmh.jar
'.*\.(RawSocketBenchmark|HttpBenchmark)\..*' -jvmArgsAppend '--enable-preview --add-modules
jdk.incubator.vector' -prof gc`

| Stage | requestResponse ops/s | pipelined ops/s | B/op |
|---|---|---|---|
| After A | 26,440 +/- 2,327 | 164,143 +/- 11,083 | 4,680 |
| After B | 26,717 +/- 872 | 163,511 +/- 8,967 | 5,968 |
| After C | 38,327 +/- 232 | 231,177 +/- 16,956 | 1,424 |

### Key files

- `uplift-synchttp/src/main/java/com/github/kjetilv/uplift/synchttp/Server.java`
- `uplift-synchttp/src/main/java/com/github/kjetilv/uplift/synchttp/DefaultServer.java`
- `uplift-synchttp/src/main/java/com/github/kjetilv/uplift/synchttp/HttpCallbackProcessor.java`
- `uplift-synchttp/src/main/java/com/github/kjetilv/uplift/synchttp/read/HttpReqReader.java`
- `uplift-synchttp/src/main/java/com/github/kjetilv/uplift/synchttp/read/Segments.java`
- `uplift-synchttp/src/main/java/com/github/kjetilv/uplift/synchttp/write/HttpResWriter.java`
- `uplift-synchttp/src/main/java/com/github/kjetilv/uplift/synchttp/write/HttpResCallbackImpl.java`
- `uplift-synchttp/src/main/java/com/github/kjetilv/uplift/synchttp/rere/HttpReq.java`
- `uplift-synchttp-jmh/PERFORMANCE.md` (full analysis and benchmark history)

### Architecture notes

Updated 2026-11-06 (review sections A, B and C):

Connections (`DefaultServer`, `Connection`):
- `Server.Processor.serve(in, out)` serves one whole connection, on its own thread.
  `DefaultServer` wraps each socket in a `Connection`, sets `TCP_NODELAY`, and closes it
  afterwards with a lingering close (shutdown output, discard input up to 1 s / 1 MiB),
  so that a TCP reset does not lose the last response
- `ServerSettings(readTimeout, backlog)`, passed to `Servers.create(address, settings)`:
  - Read timeout (default 60 s): `Connection` records when a blocking read starts; a
    background thread closes connections whose read has waited too long. Covers idle
    keep-alive and slow clients
  - Listen backlog (default 1024, capped by the OS `somaxconn`: 128 on macOS, usually 4096
    on Linux). Java's default of 50 lost most of a burst of simultaneous connects
- The accept loop logs failures and continues after a pause
- `Servers.create` removes its shutdown hook on close

Requests (`HttpReqReader`, `RequestBody`):
- `HttpCallbackProcessor` owns two `Segments` pools (thread-safe, lock-free deque). Each
  connection holds one request and one response segment until it closes. An empty pool
  allocates a new segment instead of failing
- `HttpReqReader` is per connection. It keeps bytes read past the end of a request
  and parses them as the next request (pipelining). It searches only bytes already read
- Invalid or unsupported requests throw `HttpReqException` with a status: 400 (malformed,
  bad Content-Length, both Content-Length and Transfer-Encoding), 431 (headers over 4 KiB),
  501 (unknown method, transfer coding other than chunked), 505 (version other than 1.0/1.1).
  The processor responds with that status, logs at WARN, and closes
- Bodies: `BodyBytes` for Content-Length, `ChunkedBody` for `Transfer-Encoding: chunked`.
  No body header means no body. `ChunkedBody` hands bytes read past the end of the body
  back to the reader (`leftover()`)
- `Expect: 100-continue`: `100 Continue` is sent when the handler first reads body bytes
  that are not already buffered
- After the handler: an unread body is drained up to 64 KiB; a larger one, or one the client
  holds back waiting for 100 Continue, closes the connection
- HTTP/1.1 keeps the connection unless `Connection: close`; HTTP/1.0 only with
  `Connection: keep-alive`. When closing, the response gets `connection: close` unless the
  handler set a connection header
- Header names are compared case-insensitively in place (no allocation). Header values are
  trimmed of spaces and tabs. `headerMap()` and `queryParametersMap()` join duplicates with `,`
- Query parameters are parsed with the bounded `Utils.indexOf`, and names and values are
  percent-decoded (`+` as space). `path()` is not decoded
- `HttpReq` points into the connection's buffer; it is valid until the handler returns

Responses (`HttpResCallbackImpl`):
- Status lines with reason phrases are precomputed (`StatusLine`)
- Headers are written into the response segment without allocating; ASCII is copied
  directly. Headers larger than the buffer are flushed early (no size limit)
- `body(byte[])`: a body that fits is sent in the same write as the headers, a larger one
  with a gathering write. `body(channel)` copies through the response segment
- `body(byte[])` and `body(String)` check the declared length before writing anything
- `HttpResponseCallback.finish()`: a handler that set a status but did not complete
  the response gets it sent with no body. No status, or a declared length with no
  body, gives 500 and close. A handler exception gives 500 only if nothing was written

## Build & test

```
./gradlew :uplift-synchttp:test
./gradlew :uplift-synchttp-jmh:jmh
```
