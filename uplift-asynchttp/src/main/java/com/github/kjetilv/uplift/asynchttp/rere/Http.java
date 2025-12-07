package com.github.kjetilv.uplift.asynchttp.rere;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorSpecies;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Optional;
import java.util.stream.Stream;

public final class Http {

    public static Request parse(ReadableByteChannel channel) {
        ByteVector.fromArray(SPECIES, new byte['\n'], 0);

        var extracted = extracted(channel);
        return new Request() {

            @Override
            public Line requestLine() {
                return null;
            }

            @Override
            public Headers headers() {
                return new Headers() {

                    @Override
                    public Optional<String> value(String header) {
                        return Optional.empty();
                    }

                    @Override
                    public Stream<String> values(String header) {
                        return Stream.empty();
                    }

                    @Override
                    public Stream<String> names() {
                        return Stream.empty();
                    }
                };
            }

            @Override
            public Optional<ReadableByteChannel> body() {
                return Optional.empty();
            }
        };
    }

    private Http() {
    }

    static final VectorSpecies<Byte> SPECIES = ByteVector.SPECIES_PREFERRED;

    private static ByteBuffer extracted(ReadableByteChannel channel) {
        var byteBuffer = ByteBuffer.allocateDirect(16 * 1024);
        try {
            var bytesRead = channel.read(byteBuffer);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read from " + channel, e);
        }
        return byteBuffer;
    }

    interface Request {

        Line requestLine();

        Headers headers();

        Optional<ReadableByteChannel> body();

        interface Line {

            Method method();

            String requestTarget();

            Protocol protocol();
        }

        interface Headers {

            default int count() {
                return Math.toIntExact(names().count());
            }

            Optional<String> value(String header);

            Stream<String> values(String header);

            Stream<String> names();
        }

        enum Method {

            GET,
            POST,
            PUT,
            DELETE,
            HEAD,
            OPTIONS
        }

        enum Protocol {

            HTTP_1_1;
        }
    }
}
