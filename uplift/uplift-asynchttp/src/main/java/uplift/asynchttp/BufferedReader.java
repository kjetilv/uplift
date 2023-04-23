package uplift.asynchttp;

import java.io.Closeable;

public interface BufferedReader<B> extends Closeable {

    @Override
    void close();

    B buffer(int size);

    int read(B buffer);
}
