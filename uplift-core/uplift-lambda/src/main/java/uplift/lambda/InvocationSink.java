package uplift.lambda;

@FunctionalInterface
public interface InvocationSink<Q, R> {

    Invocation<Q, R> complete(Invocation<Q, R> invocation);
}
