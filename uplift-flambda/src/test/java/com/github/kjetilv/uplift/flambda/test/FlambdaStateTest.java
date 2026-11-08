package com.github.kjetilv.uplift.flambda.test;

import module java.base;
import com.github.kjetilv.uplift.flambda.FlambdaState;
import com.github.kjetilv.uplift.flambda.LambdaReq;
import com.github.kjetilv.uplift.flambda.Outcome;
import com.github.kjetilv.uplift.lambda.RequestOut;
import com.github.kjetilv.uplift.lambda.ResponseIn;
import org.junit.jupiter.api.Test;

import static com.github.kjetilv.uplift.flambda.FlambdaState.Completion.ACCEPTED;
import static com.github.kjetilv.uplift.flambda.FlambdaState.Completion.INVALID_INVOCATION;
import static com.github.kjetilv.uplift.flambda.FlambdaState.Completion.UNKNOWN_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;

class FlambdaStateTest {

    private static final Duration SHORT = Duration.ofMillis(300);

    private static final Duration LONG = Duration.ofSeconds(5);

    @Test
    void simpleFlow() {
        var state = new FlambdaState(10);
        var apiFuture = CompletableFuture.supplyAsync(() -> state.exchange(request(), LONG));
        var delivery = state.next().orElseThrow();
        var response = new Outcome.Response(new ResponseIn(200, Map.of(), "foo", false, null));
        assertThat(state.complete(delivery.request().id(), delivery.invocationId(), response)).isEqualTo(ACCEPTED);
        assertThat(apiFuture.join()).isEqualTo(response);
        assertThat(state.pending()).isZero();
    }

    @Test
    void timeoutEndsRequest() {
        var state = new FlambdaState(10);
        assertThat(state.exchange(request(), SHORT)).isInstanceOf(Outcome.TimedOut.class);
        assertThat(state.pending()).isZero();
    }

    @Test
    void lateResponseIsRejected() {
        var state = new FlambdaState(10);
        var apiFuture = CompletableFuture.supplyAsync(() -> state.exchange(request(), SHORT));
        var delivery = state.next().orElseThrow();
        assertThat(apiFuture.join()).isInstanceOf(Outcome.TimedOut.class);
        assertThat(state.complete(delivery.request().id(), null, new Outcome.TimedOut())).isEqualTo(UNKNOWN_REQUEST);
        assertThat(state.pending()).isZero();
    }

    @Test
    void timedOutRequestIsNotDelivered() throws Exception {
        var state = new FlambdaState(10);
        assertThat(state.exchange(request(), SHORT)).isInstanceOf(Outcome.TimedOut.class);
        var next = CompletableFuture.supplyAsync(state::next);
        Thread.sleep(600);
        state.close();
        assertThat(next.join()).isEmpty();
    }

    @Test
    void wrongInvocationIdIsRejected() {
        var state = new FlambdaState(10);
        var apiFuture = CompletableFuture.supplyAsync(() -> state.exchange(request(), LONG));
        var delivery = state.next().orElseThrow();
        var id = delivery.request().id();
        assertThat(state.complete(id, "other", new Outcome.Malformed("x"))).isEqualTo(INVALID_INVOCATION);
        assertThat(state.complete(id, delivery.invocationId(), new Outcome.Malformed("x"))).isEqualTo(ACCEPTED);
        assertThat(state.complete(id, delivery.invocationId(), new Outcome.Malformed("y"))).isEqualTo(UNKNOWN_REQUEST);
        assertThat(apiFuture.join()).isEqualTo(new Outcome.Malformed("x"));
        assertThat(state.pending()).isZero();
    }

    @Test
    void fullQueueThrottles() {
        var state = new FlambdaState(1);
        var first = CompletableFuture.supplyAsync(() -> state.exchange(request(), SHORT));
        sleep(100);
        assertThat(state.exchange(request(), SHORT)).isInstanceOf(Outcome.Throttled.class);
        assertThat(first.join()).isInstanceOf(Outcome.TimedOut.class);
        assertThat(state.pending()).isZero();
    }

    @Test
    void closeEndsEverything() {
        var state = new FlambdaState(10);
        var waiting = CompletableFuture.supplyAsync(() -> state.exchange(request(), LONG));
        sleep(100);
        var start = System.nanoTime();
        state.close();
        assertThat(waiting.join()).isInstanceOf(Outcome.Closed.class);
        assertThat(state.next()).isEmpty();
        assertThat(state.exchange(request(), LONG)).isInstanceOf(Outcome.Closed.class);
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofSeconds(1));
        assertThat(state.pending()).isZero();
    }

    private static LambdaReq request() {
        return new LambdaReq(new RequestOut("GET", "/", Map.of(), Map.of(), null));
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
