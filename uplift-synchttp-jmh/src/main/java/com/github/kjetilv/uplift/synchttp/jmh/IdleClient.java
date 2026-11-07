package com.github.kjetilv.uplift.synchttp.jmh;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/// Opens connections, sends one request on each, reads the response, and keeps the connections open.
/// Prints `READY <count> <failures> <millis>`, then waits until killed.
///
/// Usage: `IdleClient <port> <connections>`
public final class IdleClient {

    private static final byte[] REQUEST = "GET / HTTP/1.1\r\nHost: h\r\n\r\n".getBytes(UTF_8);

    static void main(String[] args) throws Exception {
        var port = Integer.parseInt(args[0]);
        var count = Integer.parseInt(args[1]);
        var address = new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
        List<Socket> sockets = new ArrayList<>();
        var failures = 0;
        var start = System.nanoTime();
        for (var i = 0; i < count; i++) {
            try {
                var socket = new Socket();
                socket.connect(address, 10_000);
                socket.setSoTimeout(10_000);
                socket.getOutputStream().write(REQUEST);
                readResponse(socket.getInputStream());
                sockets.add(socket);
            } catch (IOException e) {
                if (failures++ < 3) {
                    System.err.println("Connection " + i + " failed: " + e);
                }
            }
        }
        System.out.println("READY " + sockets.size() + " " + failures + " " + (System.nanoTime() - start) / 1_000_000);
        System.out.flush();
        Thread.sleep(Long.MAX_VALUE);
    }

    private static void readResponse(InputStream in) throws IOException {
        var head = new StringBuilder();
        while (!head.toString().endsWith("\r\n\r\n")) {
            var b = in.read();
            if (b < 0) {
                throw new IOException("Closed");
            }
            head.append((char) b);
        }
        var length = head.toString().lines()
            .filter(line -> line.toLowerCase().startsWith("content-length:"))
            .mapToInt(line -> Integer.parseInt(line.substring(15).strip()))
            .findFirst()
            .orElse(0);
        in.readNBytes(length);
    }

    private IdleClient() {
    }
}
