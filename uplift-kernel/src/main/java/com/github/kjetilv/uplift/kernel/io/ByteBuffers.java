package com.github.kjetilv.uplift.kernel.io;

import module java.base;
import module uplift.flogs;
import org.slf4j.Logger;

public final class ByteBuffers {

    private static final Logger log = LoggerFactory.getLogger(ByteBuffers.class);

    public static <T> Optional<T> readBuffer(
        ByteBuffer byteBuffer,
        Function<? super Supplier<Optional<String>>, Optional<T>> fun
    ) {
        try (
            InputStream inputStream = new ByteArrayInputStream(byteBuffer.array());
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            java.io.BufferedReader lineReader = new BufferedReader(reader)
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
