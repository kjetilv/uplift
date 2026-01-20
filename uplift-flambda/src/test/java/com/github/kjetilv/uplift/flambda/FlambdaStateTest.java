package com.github.kjetilv.uplift.flambda;

import com.github.kjetilv.uplift.lambda.RequestOut;
import com.github.kjetilv.uplift.lambda.ResponseIn;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

class FlambdaStateTest {

    @Test
    void simpleFlow() {
        var lambdaState = new FlambdaState(10);
        var responses = new ArrayList<LambdaRes>();
        var request = new RequestOut(
            "GET",
            "/",
            Collections.emptyMap(),
            Collections.emptyMap(),
            null
        );
        var apiFuture = CompletableFuture.runAsync(() ->
            lambdaState.exchange(
                new LambdaReq(request),
                responses::add
            ));

        var lambdaReq = lambdaState.fetchRequest();
        assertThat(lambdaReq).isNotNull();

        var res = new LambdaRes(
            lambdaReq.id(),
            new ResponseIn(
                200,
                Collections.emptyMap(),
                "foo",
                false,
                "sdf"
            )
        );
        lambdaState.submitResponse(res);
        apiFuture.join();
        assertThat(responses).singleElement().isEqualTo(res);
    }
}