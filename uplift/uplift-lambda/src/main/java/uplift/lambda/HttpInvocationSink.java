package uplift.lambda;

import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public final class HttpInvocationSink implements InvocationSink<HttpRequest, HttpResponse<InputStream>> {

    private final Function<? super HttpRequest, ? extends CompletionStage<HttpResponse<InputStream>>> send;

    private final Supplier<Instant> time;

    HttpInvocationSink(
        Function<? super HttpRequest, ? extends CompletionStage<HttpResponse<InputStream>>> send,
        Supplier<Instant> time
    ) {
        this.send = requireNonNull(send, "send");
        this.time = requireNonNull(time, "time");
    }

    @Override
    public Invocation<HttpRequest, HttpResponse<InputStream>> complete(
        Invocation<HttpRequest, HttpResponse<InputStream>> invocation
    ) {
        return invocation.completionFuture(send, time);
    }
}
