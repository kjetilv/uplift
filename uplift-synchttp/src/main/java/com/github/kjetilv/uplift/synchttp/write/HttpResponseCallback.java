package com.github.kjetilv.uplift.synchttp.write;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Map;
import java.util.function.Consumer;

public sealed interface HttpResponseCallback permits HttpResCallbackImpl {

    static HttpResponseCallback create(WritableByteChannel out, ByteBuffer buffer) {
        return new HttpResCallbackImpl(out, buffer);
    }

    /// Responds with a status and no body.
    ///
    /// @param status Status code
    default void just(int status) {
        status(status).nobody();
    }

    Headers status(int statusCode);

    /// @return True if any part of the response has been written to the connection
    boolean committed();

    /// Called by the server after the handler returns. If the handler set a status, but did not declare a
    /// content length or complete the response, the response is sent with no body.
    ///
    /// @return True if the response is complete, and was written without failure
    boolean finish();

    interface Headers {

        default Headers headers(Map<String, Object> headers) {
            headers.forEach(this::header);
            return this;
        }

        default Headers header(String name, Object value) {
            return headers("%s: %s\r\n".formatted(name, value));
        }

        void nobody();

        Headers headers(String... literalHeaders);

        Headers contentType(String contentType);

        Body contentLength(long contentLength);

        void channel(Consumer<WritableByteChannel> channelWriter);
    }

    interface Body {

        void body(String content);

        void body(byte[] bytes);

        void body(ReadableByteChannel channel);

        void channel(Consumer<WritableByteChannel> channelWriter);
    }
}
