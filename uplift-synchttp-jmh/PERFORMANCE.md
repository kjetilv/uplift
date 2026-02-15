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
| netty | 18,962 | +/- 574 |
| upliftSynchttp | 14,834 | +/- 371 |

| Stage | uplift ops/s | Error | Gap to Netty |
|---|---|---|---|
| Baseline (no keep-alive) | 8,151 | - | 2.3x |
| Keep-alive (no pool) | 13,518 | +/- 4,674 | 1.4x |
| Keep-alive + pool + body fix | 14,420 | +/- 94 | 1.3x |
| + body(byte[]) shortcut | 15,027 | +/- 221 | 1.2x |
| + buffered headers | 14,834 | +/- 371 | 1.2x |

Throughput up 82% from baseline. Gap to Netty is ~22%.
The body(byte[]) shortcut was the most effective response-side optimization.
Buffered headers are neutral for this small-response workload but may help
with larger header sets.

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

### 5. String formatting in the hot path

- `HttpResCallbackImpl.contentType()` uses `String.formatted()`
- `HttpResCallbackImpl.header()` uses `String.formatted()`
- `HttpResCallbackImpl.statusCode()` uses string concatenation + `.getBytes()`

These generate garbage on every request.

### 6. CompletableFuture dispatch overhead

`DefaultServer.processSocket()` wraps each connection in `CompletableFuture.runAsync(..., VIRTUAL)` with a `.whenComplete` callback.
For a single-request-per-connection model, the future machinery adds overhead vs a plain virtual thread submit.

## Running

```
./gradlew :uplift-synchttp-jmh:jmh
```

With GC profiler:

```
./gradlew :uplift-synchttp-jmh:jmh --args="-wi 1 -i 1 -f 1 -t 1 -w 3s -r 3s -prof gc"
```
