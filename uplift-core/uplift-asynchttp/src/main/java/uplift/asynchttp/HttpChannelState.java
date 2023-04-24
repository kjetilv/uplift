package uplift.asynchttp;

import java.nio.ByteBuffer;
import java.util.Objects;

public final class HttpChannelState implements ChannelState {

    private final ByteBuffer requestBuffer;

    HttpChannelState(ByteBuffer requestBuffer) {
        this.requestBuffer = Objects.requireNonNull(requestBuffer, "requestBuffer");
    }

    @Override
    public ByteBuffer requestBuffer() {
        return requestBuffer;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + requestBuffer + "]";
    }
}
