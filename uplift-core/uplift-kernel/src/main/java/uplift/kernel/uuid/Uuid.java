package uplift.kernel.uuid;

import java.io.DataInput;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public interface Uuid {

    int DIGEST_LENGTH = 22;

    static String digest(UUID uuid) {
        return from(uuid).digest();
    }

    static UUID uuid(String digest) {
        return from(digest).uuid();
    }

    static Uuid from(long most, long least) {
        return from(new UUID(most, least));
    }

    static Uuid from(UUID uuid) {
        return new DefaultUuid(uuid);
    }

    static Optional<Uuid> maybeFrom(String spec) {
        return Optional.ofNullable(spec)
            .filter(s -> s.length() >= DIGEST_LENGTH)
            .filter(Uuid::isUuid)
            .map(Uuid::from);
    }

    static Uuid from(String spec) {
        int length = spec.length();
        if (length < Uuid.DIGEST_LENGTH) {
            throw new IllegalStateException("Malformed: " + spec);
        }
        return new DefaultUuid(spec);
    }

    static Uuid read(DataInput input) {
        try {
            return from(input.readLong(), input.readLong());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read uuid from " + input, e);
        }
    }

    static Uuid random() {
        return Uuid.from(UUID.randomUUID());
    }

    default String digest() {
        return Uuids.digest(uuid());
    }

    default UUID uuid() {
        return Uuid.uuid(digest());
    }

    private static boolean isUuid(String spec) {
        for (int i = 0; i < spec.length(); i++) {
            if (!isUuidChar(spec.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isUuidChar(int c) {
        return 'A' <= c && c <= 'Z' ||
            'a' <= c && c <= 'z' ||
            '0' <= c && c <= '9' ||
            c == '_' || c == '-';
    }
}
