package com.github.kjetilv.uplift.hash;

import module java.base;
import com.github.kjetilv.uplift.hash.HashKind.K256;

import static com.github.kjetilv.uplift.hash.Hashes.inputStreamHashBuilder;

public final class Main {

    static void main(String[] args) {
        var builder = inputStreamHashBuilder(HashKind.K256);
        if (args.length == 0) {
            hash(builder, System.in);
        } else {
            Arrays.stream(args)
                .map(Main::inputStream)
                .forEach(hashTo(builder));
        }
        System.out.println(builder.build().digest());
    }

    private Main() {
    }

    private static Consumer<InputStream> hashTo(HashBuilder<InputStream, K256> builder) {
        return in -> hash(builder, in);
    }

    private static void hash(HashBuilder<InputStream, K256> builder, InputStream in) {
        try (in) {
            builder.hash(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read", e);
        }
    }

    private static InputStream inputStream(String arg) {
        if ("-".equals(arg)) {
            return System.in;
        }
        try {
            return new BufferedInputStream(Files.newInputStream(Path.of(arg)));
        } catch (Exception e) {
            throw new RuntimeException("Failed to open " + arg, e);
        }
    }

}
