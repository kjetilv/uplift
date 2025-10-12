package com.github.kjetilv.uplift.kernel.io;

import module java.base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ByteBuffers {

    private static final Logger log = LoggerFactory.getLogger(ByteBuffers.class);

    public static <T> Optional<T> readBuffer(
        ByteBuffer byteBuffer,
        Function<? super Supplier<Optional<String>>, Optional<T>> fun
    ) {
        try (
            InputStream inputStream = new ByteArrayInputStream(byteBuffer.array());
            var reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            var lineReader = new BufferedReader(reader)
        ) {
            return fun.apply(nextLineSupplier(lineReader));
        } catch (Exception e) {
            log.error("Failed to parse request", e);
            return Optional.empty();
        }
    }

    private ByteBuffers() {

    }

    private static Supplier<Optional<String>> nextLineSupplier(BufferedReader lineReader) {
        return () -> {
            try {
                return Optional.ofNullable(lineReader.readLine()).map(String::trim);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read line", e);
            }
        };
    }
}
