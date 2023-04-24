package uplift.asynchttp;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.CompletionHandler;

public interface ChannelHandler<S extends ChannelState, C extends ChannelHandler<S, C>>
    extends CompletionHandler<Integer, S> {

    S channelState(ByteBuffer byteBuffer);

    C bind(AsynchronousByteChannel medium);
}
