package com.github.kjetilv.uplift.synchttp.jmh;

import com.github.kjetilv.uplift.synchttp.HttpCallbackProcessor;
import com.github.kjetilv.uplift.synchttp.HttpHandler;
import com.github.kjetilv.uplift.synchttp.ServerSettings;
import com.github.kjetilv.uplift.synchttp.Servers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.LongAdder;

import static java.nio.charset.StandardCharsets.UTF_8;

/// Server for scaling experiments. Prints `PORT <port>`, then serves until killed.
///
/// Usage: `ScalingServer <virtual|platform|virtual-raw|netty> <handler delay in ms> [backlog, default 1024]`
///
/// - `virtual`: synchttp as shipped, one virtual thread per connection
/// - `platform`: the same [HttpCallbackProcessor], one platform thread per connection, with the same
///   backlog and `TCP_NODELAY` as synchttp
/// - `virtual-raw`: like `platform`, but with virtual threads. Differs from `platform` only in thread type
/// - `netty`: the Netty comparison server, no delay supported
public final class ScalingServer {

    private static final byte[] BODY = "Hello, World!".getBytes(UTF_8);

    private static final LongAdder THREAD_FAILURES = new LongAdder();

    static void main(String[] args) throws Exception {
        var mode = args[0];
        var delay = Long.parseLong(args[1]);
        var port = switch (mode) {
            case "virtual" -> Servers.create(loopback(), settings(args))
                .open(new HttpCallbackProcessor(handler(delay)))
                .address()
                .getPort();
            case "platform" -> perConnection(new HttpCallbackProcessor(handler(delay)), Thread.ofPlatform(), settings(args));
            case "virtual-raw" -> perConnection(new HttpCallbackProcessor(handler(delay)), Thread.ofVirtual(), settings(args));
            case "netty" -> {
                if (delay > 0) {
                    throw new IllegalArgumentException("Netty mode has no delay");
                }
                yield new NettyServer(BODY, settings(args).backlog()).port();
            }
            default -> throw new IllegalArgumentException(mode);
        };
        System.out.println("PORT " + port);
        System.out.println("PID " + ProcessHandle.current().pid());
        System.out.flush();
        while (true) {
            Thread.sleep(10_000);
            if (THREAD_FAILURES.sum() > 0) {
                System.out.println("THREAD_FAILURES " + THREAD_FAILURES.sum());
                System.out.flush();
            }
        }
    }

    /// Optional third argument: listen backlog
    private static ServerSettings settings(String[] args) {
        return args.length > 2
            ? ServerSettings.DEFAULT.withBacklog(Integer.parseInt(args[2]))
            : ServerSettings.DEFAULT;
    }

    private static HttpHandler handler(long delay) {
        return (_, callback) -> {
            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
            }
            callback.status(200)
                .contentType("text/plain")
                .contentLength(BODY.length)
                .body(BODY);
        };
    }

    /// One thread per connection, from the builder. Same backlog and socket options as DefaultServer.
    @SuppressWarnings("resource")
    private static int perConnection(
        HttpCallbackProcessor processor,
        Thread.Builder builder,
        ServerSettings settings
    ) throws IOException {
        var serverChannel = ServerSocketChannel.open().bind(loopback(), settings.backlog());
        Thread.ofPlatform().name("accept").start(() -> {
            while (true) {
                SocketChannel channel;
                try {
                    channel = serverChannel.accept();
                    channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
                } catch (IOException e) {
                    System.err.println("Accept failed: " + e);
                    continue;
                }
                try {
                    builder.start(() -> {
                        try {
                            processor.serve(channel, channel);
                        } catch (Exception e) {
                            // Connection ended
                        } finally {
                            close(channel);
                        }
                    });
                } catch (Throwable e) {
                    THREAD_FAILURES.increment();
                    close(channel);
                }
            }
        });
        return ((InetSocketAddress) serverChannel.getLocalAddress()).getPort();
    }

    private static void close(SocketChannel channel) {
        try {
            channel.close();
        } catch (IOException _) {
        }
    }

    private static InetSocketAddress loopback() {
        return new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);
    }

    private ScalingServer() {
    }
}
