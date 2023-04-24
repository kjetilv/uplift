package uplift.asynchttp;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;

import static java.util.Objects.requireNonNull;

final class AsyncByteChannelBufferedWriter implements BufferedWriter<ByteBuffer> {

    private final AsynchronousByteChannel byteChannel;

    AsyncByteChannelBufferedWriter(AsynchronousByteChannel byteChannel) {
        this.byteChannel = requireNonNull(byteChannel, "out");
    }

    @Override
    public void close() {
        try {
            byteChannel.close();
        } catch (Exception e) {
            throw new IllegalStateException(this + " failed to close " + byteChannel, e);
        }
    }

    @Override
    public void write(Writable<? extends ByteBuffer> writable) {
        try {
            byteChannel.write(
                writable.buffer(),
                null,
                new WriteableHandler(writable, byteChannel, 100)
            );
        } catch (Exception e) {
            throw new IllegalStateException("Could not write to " + byteChannel, e);
        }
    }
}
