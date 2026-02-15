package com.github.kjetilv.uplift.synchttp.write;

import com.github.kjetilv.uplift.synchttp.HttpMethod;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Map;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;

public sealed interface HttpResponseCallback permits HttpResCallbackImpl {

    static HttpResponseCallback create(WritableByteChannel out, ByteBuffer buffer) {
        return new HttpResCallbackImpl(out, buffer);
    }

    default void just(int status) {
        status(status);
    }

    Headers status(int statusCode);

    interface Headers {

        default Headers headers(Map<String, Object> headers) {
            headers.forEach(this::header);
            return this;
        }

        default Headers header(String name, Object value) {
            return headers("%s: %s\r\n".formatted(name, value));
        }

        default Headers cors(HttpMethod... methods) {
            return cors(null, methods);
        }

        default void nobody() {
            contentLength(0);
        }

        Headers headers(String... literalHeaders);

        Headers contentType(String contentType);

        Headers cors(String host, HttpMethod... methods);

        Body contentLength(long contentLength);

        Body content();

        void channel(Consumer<WritableByteChannel> channelWriter);
    }

    interface Body {

        default void body(String content) {
            if (content != null && !content.isEmpty()) {
                body(content.getBytes(UTF_8));
            }
        }

        void body(byte[] bytes);

        void body(ReadableByteChannel channel);

        void channel(Consumer<WritableByteChannel> channelWriter);
    }
}
