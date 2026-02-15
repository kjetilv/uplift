# uplift-synchttp Performance Analysis

Benchmark: uplift-synchttp vs Netty 4.1.118, JMH 1.37, Java 25, GraalVM, single-threaded client.

## Results

### Before keep-alive

| Metric | uplift-synchttp | Netty | Ratio |
|---|---|---|---|
| Throughput (ops/s) | 8,151 | 19,146 | 2.3x slower |
| Allocation (B/op) | 58,243 | 17,909 | 3.3x more |

### After keep-alive (no pool)

| Benchmark | Score (ops/s) | Error |
|---|---|---|
| netty | 18,645 | +/- 686 |
| upliftSynchttp | 13,518 | +/- 4,674 |

Keep-alive improved throughput from ~8k to ~13.5k ops/s (~69% gain), but high variance
remained and a ~28% gap to Netty persisted.

### Current (keep-alive + pool + body shortcut + buffered headers)

| Benchmark | Score (ops/s) | Error |
|---|---|---|
| netty | 19,949 | +/- 151 |
| upliftSynchttp | 15,301 | +/- 412 |

| Stage | uplift ops/s | Error | Gap to Netty |
|---|---|---|---|
| Baseline (no keep-alive) | 8,151 | - | 2.3x |
| Keep-alive (no pool) | 13,518 | +/- 4,674 | 1.4x |
| Keep-alive + pool + body fix | 14,420 | +/- 94 | 1.3x |
| + body(byte[]) shortcut | 15,027 | +/- 221 | 1.2x |
| + buffered headers | 14,834 | +/- 371 | 1.2x |
| + string formatting cleanup | 15,115 | +/- 1,773 | 1.3x |
| + CompletableFuture removal | 14,631 | +/- 209 | 1.3x |
| + resSegments pool fix | 15,301 | +/- 412 | 1.3x |

Throughput up ~88% from baseline. Gap to Netty stabilizes at ~1.3x (~77% of Netty).
The two most impactful changes were keep-alive (8k -> 13.5k) and Segments pooling
(13.5k -> 14.4k). Response-side optimizations (body shortcut, buffered headers,
string formatting, CompletableFuture removal) were all within noise for this workload.

The remaining ~23% gap is likely in the fundamental I/O model: Netty's event loop
with epoll/kqueue vs blocking NIO reads on virtual threads, plus Netty's optimized
buffer pooling and zero-copy pipeline.

## Bottlenecks

### 1. No HTTP keep-alive -- IMPLEMENTED, PARTIALLY EFFECTIVE

Keep-alive loop is now in place. `Server.Processor.process()` returns `boolean`,
`DefaultServer.processChannel()` loops while the processor returns `true` and the channel
is open. `HttpReqReader.read()` returns `null` on EOF. `HttpResWriter` no longer closes
the output channel after writing. `HttpCallbackProcessor` checks for `Connection: close`.

Files changed:
- `Server.java` -- `Processor.process()` returns `boolean`
- `DefaultServer.java` -- keep-alive loop in `processChannel()`
- `HttpReqReader.java` -- `null` return on EOF (`available == 0 && done`)
- `HttpCallbackProcessor.java` -- returns `boolean`, checks `Connection: close`
- `HttpResWriter.java` -- removed `this.out` from try-with-resources

### 1a. Body-channel leak on bodyless GET requests -- FIXED

`HttpReqReader.body()` previously returned the raw socket channel when no body bytes
were buffered. On keep-alive connections with bodyless requests (GET), this could cause
the next request's data to be consumed as "body" of the previous request.

Fix: `body()` now returns `Channels.newChannel(InputStream.nullInputStream())`
when `bufferedBodyBytes <= 0`.

### 2. Per-request Arena + MemorySegment allocation -- FIXED

Previously, `HttpCallbackProcessor.process()` created a `new HttpReqReader` every request,
and `HttpReqReader.init()` called `arena.allocate(ValueLayout.JAVA_BYTE, bufferSize)` for
a fresh MemorySegment. Native memory allocation per request was expensive.

Fix: `Segments` class pools MemorySegment slices from a single pre-allocated arena.
`HttpReqReader` acquires a pooled segment via `Segments.acquire()` and releases it
via the `HttpReq.close()` callback. `HttpCallbackProcessor` owns the `Segments` pool
and closes it when the server shuts down.

### 3. Per-request direct ByteBuffer allocation in response writing -- PARTIALLY FIXED

`HttpResCallbackImpl.writeBody()` and `HttpResWriter.writeBody()` each allocated
`ByteBuffer.allocateDirect(8192)` per response.

Fix: `HttpResCallbackImpl.body(byte[])` now writes directly via `ByteBuffer.wrap(bytes)`,
bypassing the channel wrapping and direct buffer allocation entirely for byte-array bodies.
The `ReadableByteChannel` path still allocates a direct buffer per call.

### 4. Multiple small write syscalls per response -- IMPLEMENTED, NEUTRAL

`HttpResCallbackImpl` now buffers status line, headers, and CRLF into a single `ByteBuffer`
and flushes once before writing the body. This reduces syscalls from N+3 to 2 (headers + body).

Measured effect: neutral for the benchmark's small response (status + 2 headers + 13-byte body).
The kernel's TCP stack likely coalesced the small writes already. May show benefit with larger
header sets.

### 5. String formatting in the hot path -- CLEANED UP, NEUTRAL

String formatting in `HttpResCallbackImpl` was cleaned up.
Measured effect: within noise for this workload.

### 6. CompletableFuture dispatch overhead -- ADDRESSED, NEUTRAL

`DefaultServer.processSocket()` previously wrapped each connection in
`CompletableFuture.runAsync(..., VIRTUAL)` with a `.whenComplete` callback.

Measured effect: neutral. With keep-alive, the dispatch happens once per connection
rather than per request, so it is no longer in the hot path.

## Running

```
./gradlew :uplift-synchttp-jmh:jmh
```

With GC profiler:

```
./gradlew :uplift-synchttp-jmh:jmh --args="-wi 1 -i 1 -f 1 -t 1 -w 3s -r 3s -prof gc"
```
