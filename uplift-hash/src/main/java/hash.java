import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Consumer;

import static com.github.kjetilv.uplift.hash.HashBuilder.forInputStream;

void main(String[] args) {
    var builder = forInputStream(HashKind.K256);
    if (args.length == 0) {
        hash(builder, System.in);
    } else {
        Arrays.stream(args)
            .map(path -> homeIn(path))
            .map(path -> inputStream(path))
            .forEach(hashTo(builder));
    }
    System.out.println(builder.build().digest());
}

private static String homeIn(String path) {
    return path.replace("~", System.getProperty("user.home"));
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

private static Consumer<InputStream> hashTo(HashBuilder<InputStream, HashKind.K256> builder) {
    return in -> hash(builder, in);
}

private static void hash(HashBuilder<InputStream, HashKind.K256> builder, InputStream in) {
    try (in) {
        builder.hash(in);
    } catch (Exception e) {
        throw new RuntimeException("Failed to read", e);
    }
}
