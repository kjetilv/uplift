# uplift-synchttp Performance Analysis

Benchmark: uplift-synchttp vs Netty 4.1.118, JMH 1.37, Java 25, GraalVM, single-threaded client.

## Results

| Metric | uplift-synchttp | Netty | Ratio |
|---|---|---|---|
| Throughput (ops/s) | 8,151 | 19,146 | 2.3x slower |
| Allocation (B/op) | 58,243 | 17,909 | 3.3x more |

## Bottlenecks

### 1. No HTTP keep-alive (dominant)

`DefaultServer.processSocket()` closes the `SocketChannel` after every request.
The accept loop dispatches each connection to process exactly one request.
Netty keeps connections alive per HTTP/1.1 defaults.
Uplift pays TCP handshake + teardown on every request; Netty amortizes one connection across many.

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
