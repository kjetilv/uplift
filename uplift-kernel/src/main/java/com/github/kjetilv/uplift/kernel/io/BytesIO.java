package com.github.kjetilv.uplift.kernel.io;

import com.github.kjetilv.uplift.hash.Hash;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.stream.BaseStream;
import java.util.stream.IntStream;

import static com.github.kjetilv.uplift.hash.HashKind.*;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
public final class BytesIO {

    public static byte[] readInputStream(InputStream stream) {
        return readBytesFrom(null, requireNonNull(stream, "stream"));
    }

    public static byte[] readBytesFrom(Object resource, InputStream stream) {
        requireNonNull(stream, "stream");
        var buf = new byte[ATE_KAY];
        try (var baos = new ByteArrayOutputStream()) {
            var l = stream.transferTo(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read" + (resource == null ? "" : " " + resource), e);
        }
    }

    public static String readUTF8(InputStream inputStream) {
        requireNonNull(inputStream, "inputStream");
        return new String(
            readBytesFrom(inputStream, inputStream),
            StandardCharsets.UTF_8
        );
    }

    public static String readString(DataInput input) {
        var userIdLength = readInt(input);
        var bytes = new byte[userIdLength];
        try {
            input.readFully(bytes);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read string", e);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static Instant readInstant(DataInput input) {
        try {
            return Instant.ofEpochSecond(requireNonNull(input, "input").readLong());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read instant from " + input, e);
        }
    }

    public static int writeInstant(DataOutput output, Instant instant) {
        return writeLong(output, instant.getEpochSecond());
    }

    public static List<Hash<K128>> readHashes128(DataInput input) {
        var count = readInt(input);
        return IntStream.range(0, count)
            .mapToObj(i -> Hash.of(input, K128))
            .toList();
    }

    public static int writeLong(DataOutput output, Long value) {
        try {
            output.writeLong(requireNonNull(value, "value"));
            return 8;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write " + value, e);
        }
    }

    public static int writeEpoch(DataOutput output, Instant instant) {
        return writeLong(output, requireNonNull(instant, "instant").getEpochSecond());
    }

    public static int writeString(DataOutput output, String value) {
        try {
            var bytes = requireNonNull(value, "value")
                .getBytes(StandardCharsets.UTF_8);
            output.writeInt(bytes.length);
            output.write(bytes);
            return 4 + bytes.length;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write " + value, e);
        }
    }

    public static int writeHash128(DataOutput output, Hash<K128> hash) {
        requireNonNull(hash, "hash");
        return writeLong(output, hash.ls()[0]) +
               writeLong(output, hash.ls()[1]);
    }

    public static int writeHashes128(DataOutput output, List<? extends Hash<K128>> list) {
        var length = writeInt(output, requireNonNull(list, "list").size());
        var data = list.stream()
            .mapToInt(hash ->
                writeHash128(output, hash))
            .sum();
        return length + data;
    }

    public static int writeHash256(DataOutput output, Hash<K256> hash) {
        requireNonNull(hash, "hash");
        return writeLong(output, hash.ls()[0]) +
               writeLong(output, hash.ls()[1]) +
               writeLong(output, hash.ls()[2]) +
               writeLong(output, hash.ls()[3]);
    }

    public static int writeWritables(DataOutput output, Collection<? extends BinaryWritable> list) {
        if (list == null) {
            throw new IllegalArgumentException("Null list");
        }
        var len = writeInt(output, list.size());
        var data = list.stream()
            .mapToInt(idOut ->
                idOut.writeTo(output))
            .sum();
        return len + data;
    }

    public static String stringFromBase64(String base64) {
        return new String(fromBase64(base64), ISO_8859_1);
    }

    public static byte[] fromBase64(String base64) {
        try {
            return Base64.getDecoder().decode(base64);
        } catch (Exception e) {
            throw new IllegalStateException("Could not decode `" + base64 + "`", e);
        }
    }

    public static String toBase64(byte[] body) {
        try {
            return new String(Base64.getEncoder().encode(body), ISO_8859_1);
        } catch (Exception e) {
            throw new IllegalStateException("Could not encode " + body.length + " bytes", e);
        }
    }

    public static byte[] nonNull(byte[] body) {
        return body == null || body.length == 0 ? NOBODY : body;
    }

    public static String toBase64(InputStream body) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        var base64Encoder = Base64.getEncoder().wrap(byteArrayOutputStream);
        try {
            body.transferTo(base64Encoder);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to transfer body to base64", e);
        }
        return byteArrayOutputStream.toString(US_ASCII);
    }

    private BytesIO() {

    }

    private static final byte[] NOBODY = {};

    private static final int ATE_KAY = 8192;

    private static int readInt(DataInput input) {
        try {
            return input.readInt();
        } catch (IOException e) {
            throw new IllegalStateException("Could not read count", e);
        }
    }

    private static byte[] readTo(InputStream stream, byte[] buf, ByteArrayOutputStream baos) {
        var bytesRead = 0;
        try {
            while (true) {
                var read = stream.read(buf);
                if (read < 0) {
                    bytesRead += read;
                    baos.flush();
                    return baos.toByteArray();
                }
                baos.write(buf, 0, read);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Read failed after " + bytesRead + " bytes", e);
        }
    }

    private static int writeInt(DataOutput output, int size) {
        try {
            output.writeInt(size);
            return 4;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write " + size + "tracks", e);
        }
    }
}
