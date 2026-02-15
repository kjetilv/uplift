# uplift-synchttp Performance Analysis

Benchmark: uplift-synchttp vs Netty 4.1.118, JMH 1.37, Java 25, GraalVM, single-threaded client.

## Results

### Before keep-alive

| Metric | uplift-synchttp | Netty | Ratio |
|---|---|---|---|
| Throughput (ops/s) | 8,151 | 19,146 | 2.3x slower |
| Allocation (B/op) | 58,243 | 17,909 | 3.3x more |

### After keep-alive (current)

| Benchmark | Score (ops/s) | Error |
|---|---|---|
| netty | 18,645 | +/- 686 |
| upliftSynchttp | 13,518 | +/- 4,674 |

Keep-alive improved throughput from ~8k to ~13.5k ops/s (~69% gain), but high variance
remains and a ~28% gap to Netty persists. See bottleneck #1 status and #1a below.

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

### 1a. Suspected body-channel leak on bodyless GET requests (OPEN)

The benchmark client sends GET requests with no body. `HttpReqReader.body()` returns
the raw `ReadableByteChannel` (the socket itself) when there are no buffered body bytes
(`bufferedBodyBytes <= 0`). On a keep-alive connection, the next call to `HttpReqReader.read()`
may then find its data partially consumed if any code drained bytes from the channel as
"body" of the previous request.

For bodyless requests (no `Content-Length` or `Content-Length: 0`), `body()` should
return `null` or an empty channel -- never the raw socket channel. This likely explains
both the high variance and the throughput gap.

**Investigation needed:** trace what happens to the `ReadableByteChannel` returned by
`body()` when the benchmark handler calls `callback.status(200).contentType(...).body(...)`.
If `HttpResponseCallback` or the handler ever reads from it, bytes from the next pipelined
request are consumed and lost.

### 2. Per-request Arena + MemorySegment allocation

`HttpCallbackProcessor.process()` creates a `new HttpReqReader` every request.
`HttpReqReader.init()` calls `arena.allocate(ValueLayout.JAVA_BYTE, bufferSize)` for a fresh MemorySegment.
Native memory allocation per request is expensive compared to pooled buffers.

### 3. Per-request direct ByteBuffer allocation in response writing

`HttpResCallbackImpl.writeBody()` and `HttpResWriter.writeBody()` each allocate `ByteBuffer.allocateDirect(8192)` per response.
Direct buffer allocation involves a native call, much more expensive than heap or pooled buffers.

### 4. Multiple small write syscalls per response

`HttpResCallbackImpl` issues separate channel writes for:
- Status line
- Each header
- CRLF separator
- Body

Each `out.write()` is a syscall. Netty batches the entire response into a single `writeAndFlush`.

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
