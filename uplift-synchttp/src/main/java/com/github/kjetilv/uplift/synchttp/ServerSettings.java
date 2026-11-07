package com.github.kjetilv.uplift.synchttp;

import module java.base;

/// Settings for a [server][Servers].
///
/// @param readTimeout Max time to wait for input, including idle time between requests on a keep-alive
///                    connection. A connection that waits longer is closed. Null, zero or negative gives
///                    the default, 60 seconds.
/// @param backlog     Max number of connections the operating system queues before the server accepts them.
///                    Connections beyond that may be lost when many clients connect at once. The operating
///                    system caps the value, at `kern.ipc.somaxconn` on macOS and `net.core.somaxconn` on
///                    Linux. Zero or negative gives the default, 1024.
public record ServerSettings(Duration readTimeout, int backlog) {

    public static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(60);

    public static final int DEFAULT_BACKLOG = 1024;

    public static final ServerSettings DEFAULT = new ServerSettings(null, 0);

    public ServerSettings {
        readTimeout = readTimeout == null || readTimeout.isNegative() || readTimeout.isZero()
            ? DEFAULT_READ_TIMEOUT
            : readTimeout;
        backlog = backlog > 0 ? backlog : DEFAULT_BACKLOG;
    }

    public ServerSettings withReadTimeout(Duration readTimeout) {
        return new ServerSettings(readTimeout, backlog);
    }

    public ServerSettings withBacklog(int backlog) {
        return new ServerSettings(readTimeout, backlog);
    }
}
