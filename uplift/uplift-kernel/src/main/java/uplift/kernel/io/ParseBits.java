package uplift.kernel.io;

import java.util.Locale;
import java.util.Optional;
import java.util.OptionalLong;

import uplift.kernel.uuid.Uuid;

public final class ParseBits {

    public static long parseLong(CharSequence s) {
        int length = s.length();
        if (length > MAX_LONG_LENGTH) {
            throw new IllegalStateException("Not a long: " + length);
        }
        long value = 0;
        int radix = 1;
        int lastIdx = length - 1;
        for (int i = lastIdx; i >= 0; i--) {
            long num = s.charAt(i) - '0';
            if (0 <= num && num <= 9) {
                value += num * radix;
            } else {
                throw new IllegalStateException("Not a number: " + s);
            }
            radix *= 10;
        }
        return value;
    }

    public static OptionalLong maybeParseLong(CharSequence s) {
        int length = s.length();
        if (length > MAX_LONG_LENGTH) {
            return OptionalLong.empty();
        }
        long value = 0;
        int radix = 1;
        int lastIdx = length - 1;
        for (int i = lastIdx; i >= 0; i--) {
            long num = s.charAt(i) - '0';
            if (0 <= num && num <= 9) {
                value += num * radix;
            } else {
                return OptionalLong.empty();
            }
            radix *= 10;
        }
        return OptionalLong.of(value);
    }

    public static Optional<String> tailString(String line, String prefix) {
        return Optional.ofNullable(line)
            .filter(l -> l.toLowerCase(Locale.ROOT).startsWith(prefix))
            .flatMap(l -> tailString(l, prefix.length()));
    }

    public static Optional<String> tailString(String line, int index) {
        int lastIndex = line.length() - 1;
        return index < lastIndex
            ? Optional.of(line.substring(index))
            : Optional.empty();
    }

    public static Optional<String> headString(String s, int index) {
        int lastIndex = s.length() - 1;
        return index < lastIndex
            ? Optional.of(s.substring(0, index))
            : Optional.empty();
    }

    public static Optional<Uuid> parseUUID(String digest) {
        if (digest.length() < Uuid.DIGEST_LENGTH) {
            return Optional.empty();
        }
        try {
            return Optional.of(Uuid.from(digest));
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected parse error: " + digest, e);
        }
    }

    private ParseBits() {
    }

    private static final int MAX_LONG_LENGTH = String.valueOf(Long.MAX_VALUE).length();

    private static boolean uuidLike(int c) {
        return c == '-' || Character.isLetterOrDigit(c);
    }
}
