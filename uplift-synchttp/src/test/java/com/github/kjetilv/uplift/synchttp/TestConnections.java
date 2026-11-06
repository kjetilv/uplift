package com.github.kjetilv.uplift.synchttp;

import module java.base;

import static java.nio.charset.StandardCharsets.UTF_8;

/// Socket helpers for server tests
final class TestConnections {

    static String withServer(HttpHandler handler, Function<Server.Open, String> action) {
        return withServer(null, handler, action);
    }

    static String withServer(Duration readTimeout, HttpHandler handler, Function<Server.Open, String> action) {
        var address = new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);
        try (var server = Servers.create(address, readTimeout).open(new HttpCallbackProcessor(handler))) {
            return action.apply(server);
        }
    }

    /// Writes all data, with pauses, half-closes the connection, and reads until the server closes.
    static String halfClosedExchange(Server.Open server, String... writes) {
        try (var socket = connect(server)) {
            var out = socket.getOutputStream();
            for (var write : writes) {
                out.write(write.getBytes(UTF_8));
                out.flush();
                pause();
            }
            socket.shutdownOutput();
            return new String(socket.getInputStream().readAllBytes(), UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /// Writes data, with pauses, and reads until the server closes (`<EOF>`) or the read times out
    /// (`<TIMEOUT>`). Stops writing if the server has closed the connection.
    static String openExchange(Server.Open server, String... writes) {
        try (var socket = connect(server)) {
            var out = socket.getOutputStream();
            for (var write : writes) {
                try {
                    out.write(write.getBytes(UTF_8));
                    out.flush();
                } catch (IOException e) {
                    break;
                }
                pause();
            }
            return readUntilClosed(socket);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static String readUntilClosed(Socket socket) throws IOException {
        var in = socket.getInputStream();
        var received = new ByteArrayOutputStream();
        var buffer = new byte[4096];
        try {
            while (true) {
                var read = in.read(buffer);
                if (read < 0) {
                    return received.toString(UTF_8) + "<EOF>";
                }
                received.write(buffer, 0, read);
            }
        } catch (SocketTimeoutException e) {
            return received.toString(UTF_8) + "<TIMEOUT>";
        } catch (SocketException e) {
            return received.toString(UTF_8) + "<EOF>";
        }
    }

    /// Reads up to and including the empty line after the headers
    static String readHead(InputStream in) throws IOException {
        var head = new StringBuilder();
        while (!head.toString().endsWith("\r\n\r\n")) {
            var b = in.read();
            if (b < 0) {
                return head + "<EOF>";
            }
            head.append((char) b);
        }
        return head.toString();
    }

    static Socket connect(Server.Open server) throws IOException {
        var socket = new Socket();
        socket.connect(server.address());
        socket.setSoTimeout(3000);
        return socket;
    }

    static String get(String path) {
        return "GET " + path + " HTTP/1.1\r\nHost: h\r\n\r\n";
    }

    static void pause() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private TestConnections() {
    }
}
