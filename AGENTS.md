# uplift project notes

## HTTP keep-alive for uplift-synchttp -- COMPLETE

### Status

All identified bottlenecks have been addressed. Latest benchmark:

| Benchmark | Score (ops/s) | Error |
|---|---|---|
| Netty | 19,949 | +/- 151 |
| upliftSynchttp | 15,301 | +/- 412 |

Throughput up ~88% from baseline (8,151 -> 15,301). Gap to Netty stabilizes at ~1.3x.
The remaining delta is fundamental: Netty's event loop with epoll/kqueue vs blocking
NIO on virtual threads, plus Netty's optimized buffer pooling and zero-copy pipeline.

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

- `HttpCallbackProcessor` owns two `Segments` pools: `reqSegments` for request
  parsing and `resSegments` for response header buffering
- Request segments are released via the `HttpReq.close()` callback (Runnable closer)
- Response segments are released explicitly via `resSegments.release(pooled)` in
  the finally block of `HttpCallbackProcessor.process()`
- Both pools are closed when `HttpCallbackProcessor.close()` is called
- `HttpResCallbackImpl` buffers all header writes into a ByteBuffer, then flushes
  once via `flushHeaders()` when transitioning to body writing (`headersComplete` flag)
- `contentLength(0)` skips the content-length header but still writes the CRLF
  separator to terminate headers

### Pre-existing issue

`uplift-flambda` has a compilation error unrelated to keep-alive:
`CorsSettings.applyTo()` signature mismatch at `Flambda.java:147`.

## Build & test

```
./gradlew :uplift-synchttp:test
./gradlew :uplift-synchttp-jmh:jmh
```
