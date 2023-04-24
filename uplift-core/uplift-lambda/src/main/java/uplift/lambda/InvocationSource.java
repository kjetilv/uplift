package uplift.lambda;

import java.io.Closeable;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

@FunctionalInterface
public interface InvocationSource<Q, R> extends Closeable {

    @Override
    default void close() {
    }

    Optional<CompletionStage<Invocation<Q, R>>> next();
}
