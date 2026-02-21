package com.github.kjetilv.uplift.lambda;

import module java.base;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.util.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.kjetilv.uplift.lambda.LambdaResult.status;
import static java.util.Map.entry;

@SuppressWarnings("unused")
public abstract class LambdaHandlerSupport implements LambdaHandler {

    private static final Logger log = LoggerFactory.getLogger(LambdaHandlerSupport.class);

    @Override
    public final LambdaResult handle(LambdaPayload lambdaPayload) {
        var lambdaResult = lambdaResult(lambdaPayload);
        return lambdaResult.orElseGet(() ->
            error(lambdaPayload, BAD_REQUEST, "{}: No result: {} {}", this, lambdaPayload)
        );
    }

    protected abstract Optional<LambdaResult> lambdaResult(LambdaPayload payload);

    protected static final int OK = 200;

    protected static final int BAD_REQUEST = 400;

    protected static final int UNAUTHORIZED = 401;

    protected static Supplier<LambdaResult> errorSupplier(int statusCode, String error, Object... args) {
        return () ->
            error(null, statusCode, error, args);
    }

    protected static LambdaResult error(
        LambdaPayload lambdaPayload,
        int statusCode,
        String error,
        Object... args
    ) {
        var result = status(statusCode);
        log.error(error, args);
        log.debug("Failing lambda request {} with {}", source(lambdaPayload), result);
        return result;
    }

    protected static LambdaResult lambdaResult(byte[] body) {
        return LambdaResult.json(OK, body, entry("Content-Type", "application/json"));
    }

    private static String source(LambdaPayload lambdaPayload) {
        if (lambdaPayload == null) {
            return "<no payload>";
        }
        try {
            return Json.instance().write(lambdaPayload.source());
        } catch (Exception e) {
            return lambdaPayload + " (" + Throwables.summary(e) + ")";
        }
    }
}
