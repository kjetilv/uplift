package com.github.kjetilv.uplift.kernel.io;

import com.github.kjetilv.uplift.uuid.Uuid;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
public final class BytesIO {

    public static byte[] readInputStream(InputStream stream) {
        return readBytesFrom(null, requireNonNull(stream, "stream"));
    }

    public static byte[] readBytesFrom(Object resource, InputStream stream) {
        requireNonNull(stream, "stream");
        byte[] buf = new byte[ATE_KAY];
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            return readTo(stream, buf, baos);
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

    public static String readString(DataInput input) throws IOException {
        int userIdLength = readInt(input);
        byte[] bytes = new byte[userIdLength];
        input.readFully(bytes);
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

    public static List<Uuid> readUuids(DataInput input) {
        int count = readInt(input);
        return IntStream.range(0, count)
            .mapToObj(i -> readUuid(input))
            .toList();
    }

    public static Uuid readUuid(DataInput input) {
        try {
            requireNonNull(input, "input");
            return Uuid.from(input.readLong(), input.readLong());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
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
            byte[] bytes = requireNonNull(value, "value")
                .getBytes(StandardCharsets.UTF_8);
            output.writeInt(bytes.length);
            output.write(bytes);
            return 4 + bytes.length;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write " + value, e);
        }
    }

    public static int writeUuid(DataOutput output, Uuid uuid) {
        return writeUuid(output, requireNonNull(uuid, "uuid").uuid());
    }

    public static int writeUuid(DataOutput output, UUID uuid) {
        requireNonNull(uuid, "uuid");
        return writeLong(output, uuid.getMostSignificantBits()) +
               writeLong(output, uuid.getLeastSignificantBits());
    }

    public static int writeUuids(DataOutput output, List<? extends Uuid> list) {
        int length = writeInt(output, requireNonNull(list, "list").size());
        int data = list.stream()
            .mapToInt(track ->
                writeUuid(output, track))
            .sum();
        return length + data;
    }

    public static int writeWritables(DataOutput output, Collection<? extends BinaryWritable> list) {
        if (list == null) {
            throw new IllegalArgumentException("Null list");
        }
        int len = writeInt(output, list.size());
        int data = list.stream()
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
            return DECODER.decode(base64);
        } catch (Exception e) {
            throw new IllegalStateException("Could not decode `" + base64 + "`", e);
        }
    }

    public static String toBase64(byte[] body) {
        try {
            return new String(ENCODER.encode(body), ISO_8859_1);
        } catch (Exception e) {
            throw new IllegalStateException("Could not encode " + body.length + " bytes", e);
        }
    }

    public static byte[] nonNull(byte[] body) {
        return body == null || body.length == 0 ? NOBODY : body;
    }

    private BytesIO() {

    }

    private static final byte[] NOBODY = {};

    private static final int ATE_KAY = 8192;

    private static final Base64.Encoder ENCODER = Base64.getEncoder();

    private static final Base64.Decoder DECODER = Base64.getDecoder();

    private static int readInt(DataInput input) {
        try {
            return input.readInt();
        } catch (IOException e) {
            throw new IllegalStateException("Could not read count", e);
        }
    }

    private static byte[] readTo(InputStream stream, byte[] buf, ByteArrayOutputStream baos) {
        int bytesRead = 0;
        try {
            while (true) {
                int read = stream.read(buf);
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
