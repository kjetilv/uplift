# uplift-synchttp Performance Analysis

## 2026-11-06: review sections A, B and C

Measured on macOS ARM over loopback, JMH 1 fork, 3 x 5 s warmup, 5 x 5 s measurement, `-prof gc`.
`RawSocketBenchmark` was added: one keep-alive connection with a raw socket client, browser-like
headers, and a handler that reads headers and query parameters.

| Stage | HttpBenchmark uplift | HttpBenchmark netty | RawSocket requestResponse | RawSocket pipelined | B/op (raw) |
|---|---|---|---|---|---|
| After A (correctness) | 14,549 +/- 577 | 18,053 +/- 746 | 26,440 +/- 2,327 | 164,143 +/- 11,083 | 4,680 |
| After B (protocol) | 14,254 +/- 1,281 | 17,829 +/- 1,855 | 26,717 +/- 872 | 163,511 +/- 8,967 | 5,968 |
| After C (performance) | 17,102 +/- 946 | 17,990 +/- 1,462 | 38,327 +/- 232 | 231,177 +/- 16,956 | 1,424 |

C combined these changes, measured together, so their separate effects are not known:
- Header names compared in place. Before, the first lookup by name allocated a direct
  ByteBuffer, an AtomicReference and a lambda for each header. GC time in `pipelined` fell from
  6.1 s to 0.012 s per 25 s.
- Response headers written into the response segment without allocation; precomputed status lines.
- A body that fits is sent in the same write as the headers; larger bodies use a gathering write.
- `TCP_NODELAY` on accepted sockets.
- Channel bodies copied through the response segment instead of a new direct buffer.

## 2026-11-07: does synchttp scale because of virtual threads?

### Setup

One macOS ARM machine, 14 cores, 48 GB, over loopback. Client and server share the cores.
Java 25, `-Xms2g -Xmx2g -XX:+UseG1GC`, same flags for every server. `kern.ipc.somaxconn` set
to 1024, and every server bound with a backlog of 1024.

Servers, each in its own JVM, started with `ScalingServer <mode> <delay ms> [backlog]`:

| Mode | What it is |
|---|---|
| `virtual` | synchttp as shipped (`Servers.create`, one virtual thread per connection) |
| `virtual-raw` | plain accept loop, one virtual thread per connection, same `HttpCallbackProcessor` |
| `platform` | the same accept loop, one platform thread per connection |
| `netty` | the Netty comparison server (`NettyServer`) |

`virtual-raw` and `platform` differ only in thread type. The handler returns "Hello, World!",
optionally after `Thread.sleep(delay)` to stand in for blocking I/O such as an S3 call.

Load: `h2load --h1`, up to 100 connections per process, processes started 0.25 s apart, with
warm-up periods that end together so all processes measure the same 10 s window. Connections
are opened gradually because `h2load` does not retry a connection that fails, and a burst larger
than the listen queue loses clients. The established connection count was read from the server
with `lsof` during each run. Latency percentiles come from `h2load --log-file`. CPU per request
is cumulative server process CPU time over the window, divided by requests.

### E1: trivial handler

All servers reach 86k-97k req/s up to 4,096 connections. That limit is the client and loopback,
so this test compares CPU and latency only.

| Connections | CPU/request virtual | platform | netty | p99.9 virtual / platform / netty |
|---|---|---|---|---|
| 1 | 14.4 us | 7.6 us | 9.4 us | 0.1 / 0.06 / 0.09 ms |
| 64 | 31 us | 16 us | 18 us | 2.8 / 3.4 / 5.0 ms |
| 1,024 | 33 us | 15 us | 16 us | 18 / 14 / 15 ms |
| 4,096 | 32 us | 15 us | 16 us | 229 / 68 / 202 ms |

At one connection: virtual 30k req/s, platform 38k, netty 34k. Platform threads held 4,096 and
10,000 connections (4,127 and 10,031 OS threads) without trouble.

### E2: handler that blocks for 10 ms

Throughput as a fraction of the ideal, connections / 10 ms. Two runs each, order reversed.

| Connections | virtual | virtual-raw | platform |
|---|---|---|---|
| 64 | 0.74-0.80, 93-97 us/req | 0.80, 86-95 us/req | 0.89, 50-52 us/req |
| 256 | 0.81-0.89, 79-126 us/req | 0.88, 81-86 us/req | 0.89, 37-38 us/req |
| 1,024 | 0.69-0.71, 100-112 us/req | 0.69-0.72, 106-110 us/req | 0.66, 127-131 us/req |
| 4,096 | 80k-82k req/s, p99 59 ms, 4,096 served | 80k-82k req/s, p99 57-59 ms | 52k-53k req/s, p99 154-193 ms, 3,000-3,500 served |

At 4,096 the platform server did not get all connections established: creating a thread per
accepted connection slows the accept loop under load.

### E3: idle connections

10,000 keep-alive connections, opened one at a time, server RSS after `GC.run`:

| | virtual | platform | netty |
|---|---|---|---|
| RSS per connection | 9-12 KB | 41-43 KB | 3 KB |
| OS threads | ~40 | ~10,030 | ~60 |
| Time to open 10,000 connections | 1.4 s | 13 s | 1.6 s |

Above about 10,230 connections both thread types failed with "Too many open files": the JVM caps
open files at 10,240 on macOS. So the macOS limit of 12,288 threads per process was never reached.

### Pinning

JFR `jdk.VirtualThreadPinned` with a 0 ms threshold, during E2 at 4,096 connections: 323 events,
all in the first second, from class loading and class initialization (157 ms in total), plus one
0.75 us event inside JFR itself. No pinning in steady state.

### Listen backlog

Burst of simultaneous connects from 4 `h2load` threads, connections established:

| Burst | backlog 50 (Java default) | backlog 1024 | netty, backlog 128 |
|---|---|---|---|
| 1,024 | 14-16% | 100% | 49-56% |
| 4,096 | 51-72% | 71-86% | 24-62% |

The default is now 1024 (`ServerSettings`). The OS caps it at `somaxconn` (128 on macOS by
default, usually 4096 on Linux).

### Conclusion

- `virtual` and `virtual-raw` perform the same, so synchttp's own code adds no measurable cost.
- Up to about 256 connections, platform threads are better: half the CPU per request, and about
  1 ms lower latency with blocking handlers.
- From about 1,024 blocked handlers, virtual threads cost less CPU per request. At 4,096 they give
  1.5x the throughput, a third of the p99 latency, and accept every connection.
- Virtual threads use about a quarter of the memory per idle connection, and a fixed number of
  OS threads. Netty uses less memory still.
- At 10,000 active connections every server, Netty included, falls to p99 above 1 s. That point
  measures this machine, not the servers.

Limits: one machine, loopback, client on the same cores; `Thread.sleep` instead of real I/O
(a sleeping virtual thread uses a timer, not the I/O poller); client ports were cleared with a
fixed 35 s wait between runs, not checked; E1 latencies are from one run each. Linux has
different thread limits and uses epoll.

The sections below describe earlier work.


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
