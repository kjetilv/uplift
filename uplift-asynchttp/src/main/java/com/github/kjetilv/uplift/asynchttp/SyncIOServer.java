package com.github.kjetilv.uplift.asynchttp;

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import static com.github.kjetilv.uplift.asynchttp.IOServer.resolveAddress;

public interface SyncIOServer extends IOServer {

    static SyncIOServer create() {
        return create(0);
    }

    static SyncIOServer create(int port) {
        return new DefaultSyncIOServer(resolveAddress(port));
    }

    SyncIOServer run(Handler handler);

    interface Handler {

        void run(ReadableByteChannel in, WritableByteChannel out);
    }
}
