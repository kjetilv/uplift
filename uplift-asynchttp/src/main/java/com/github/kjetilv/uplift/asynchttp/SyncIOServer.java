package com.github.kjetilv.uplift.asynchttp;

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import static com.github.kjetilv.uplift.asynchttp.IOServer.requestBufferSize;
import static com.github.kjetilv.uplift.asynchttp.IOServer.resolveAddress;

public interface SyncIOServer extends IOServer {

    static SyncIOServer create() {
        return create(0);
    }

    static SyncIOServer create(int port) {
        return create(port, 0);
    }

    static SyncIOServer create(int port, int requestBufferSize) {
        return new DefaultSyncIOServer(
            resolveAddress(port),
            requestBufferSize(requestBufferSize)
        );
    }

    SyncIOServer run(Handler handler);

    interface Handler {

        void run(ReadableByteChannel in, WritableByteChannel out);
    }
}
