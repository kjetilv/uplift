package uplift.asynchttp;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousByteChannel;
import java.util.function.Function;

public interface IOServer extends Closeable {

    void join();

    @Override
    void close();

    InetSocketAddress address();

    <S extends ChannelState, C extends ChannelHandler<S, C>> IOServer run(
        Function<? super AsynchronousByteChannel, ? extends ChannelHandler<S, C>> channelHandler
    );
}
