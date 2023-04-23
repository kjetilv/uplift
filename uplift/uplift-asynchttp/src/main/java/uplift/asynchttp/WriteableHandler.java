package uplift.asynchttp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class WriteableHandler implements CompletionHandler<Integer, Object> {

    private static final Logger log = LoggerFactory.getLogger(WriteableHandler.class);

    private final LongAdder writtenTracker;

    private final int expectedTotal;

    private final ByteBuffer buffer;

    private final AsynchronousByteChannel byteChannel;

    private final AtomicInteger triesLeft;

    private final int retries;

    WriteableHandler(Writable<? extends ByteBuffer> writable, AsynchronousByteChannel byteChannel, int retries) {
        this.buffer = writable.buffer();
        this.byteChannel = byteChannel;
        this.retries = Math.max(MIN_RETRIES, retries);
        this.triesLeft = new AtomicInteger(retries);
        this.writtenTracker = new LongAdder();
        this.expectedTotal = writable.size();
    }

    @Override
    public void completed(Integer result, Object attachment) {
        long total = trackWrites(result);
        if (total < this.expectedTotal && buffer.hasRemaining()) {
            if (triesLeft.getAndDecrement() > 0) {
                byteChannel.write(buffer, attachment, this);
            } else {
                throw new IllegalStateException(
                    "Aborted write, " + total + " bytes written in " + retries + " retries");
            }
        } else {
            log.debug("Written: {}", this);
        }
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        if (
            exc instanceof IOException && "Broken pipe".equals(exc.getMessage()) ||
                exc instanceof AsynchronousCloseException as
        ) {
            log.debug("Client closed connection: {}", exc.toString());
        } else {
            log.error("{} failed", this, exc);
        }
    }

    private long trackWrites(Integer result) {
        writtenTracker.add(result != null ? result : 0);
        return writtenTracker.longValue();
    }

    private static final int MIN_RETRIES = 10;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + writtenTracker.longValue() + "/" + expectedTotal + "]";
    }
}
