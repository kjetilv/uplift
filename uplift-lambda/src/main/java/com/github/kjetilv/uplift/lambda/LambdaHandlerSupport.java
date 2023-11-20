package com.github.kjetilv.uplift.lambda;

import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.kjetilv.uplift.lambda.LambdaResult.status;
import static java.util.Map.entry;

public abstract class LambdaHandlerSupport implements LambdaHandler {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(LambdaHandlerSupport.class);

    @Override
    public final LambdaResult handle(LambdaPayload lambdaPayload) {
        return result(lambdaPayload).orElseGet(() ->
            error(BAD_REQUEST, "{}: No result: {} {}" , this, lambdaPayload)
        );
    }

    protected abstract Optional<LambdaResult> result(LambdaPayload payload);

    protected static final int OK = 200;

    protected static final int BAD_REQUEST = 400;

    protected static final int UNAUTHORIZED = 401;

    protected static Supplier<LambdaResult> errorSupplier(int statusCode, String error, Object... args) {
        return () ->
            error(statusCode, error, args);
    }

    protected static LambdaResult error(int statusCode, String error, Object... args) {
        LambdaResult result = status(statusCode);
        try {
            return result;
        } finally {
            log.error(error, args);
            log.error("Failing with {}", result);
        }
    }

    protected static LambdaResult result(byte[] body) {
        return LambdaResult.json(OK, body, entry("Content-Type", "application/json"));
    }

    protected static Optional<LambdaResult> handleBody(String body, Function<String, LambdaResult> bodyHandler) {
        return Optional.ofNullable(body).map(bodyHandler);
    }

    protected static Optional<LambdaResult> handleQuery(
        Map<?, ?> params,
        Function<Map<?, ?>, LambdaResult> queryHandler
    ) {
        return Optional.ofNullable(params).map(queryHandler);
    }
}
