package main;

import module java.base;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;

import static com.github.kjetilv.uplift.hash.HashBuilder.forInputStream;

public class Hash {

    void main(String[] args) {
        var builder = forInputStream(HashKind.K256);
        if (args.length == 0) {
            hashTo(builder, System.in);
        } else {
            Arrays.stream(args)
                .map(path ->
                    path.replace("~", System.getProperty("user.home")))
                .map(Hash::toInputStream)
                .forEach(in ->
                    hashTo(builder, in));
        }
        IO.println(builder.build().digest());
    }

    private static InputStream toInputStream(String arg) {
        if ("-".equals(arg)) {
            return System.in;
        }
        try {
            return new BufferedInputStream(Files.newInputStream(Path.of(arg)));
        } catch (Exception e) {
            throw new RuntimeException("Failed to open " + arg, e);
        }
    }

    private static void hashTo(HashBuilder<InputStream, HashKind.K256> builder, InputStream in) {
        try (in) {
            builder.hash(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read", e);
        }
    }
}
