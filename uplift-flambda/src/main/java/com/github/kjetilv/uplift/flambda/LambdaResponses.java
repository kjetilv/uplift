package com.github.kjetilv.uplift.flambda;

import module java.base;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.lambda.ResponseIn;
import com.github.kjetilv.uplift.lambda.ResponseInRW;

/// Interprets what a Lambda posts to the Runtime API.
final class LambdaResponses {

    /// A response with `statusCode` is used as it is. Other valid JSON is wrapped as API Gateway does for payload
    /// format 2.0: status 200, content type `application/json`, and the function's output as the body.
    ///
    /// @param bytes Posted response
    /// @return Outcome
    static Outcome response(byte[] bytes) {
        Object json;
        try {
            json = Json.instance().read(bytes);
        } catch (Exception e) {
            return new Outcome.Malformed("Response is not JSON: " + e.getMessage());
        }
        if (json instanceof Map<?, ?> map && map.containsKey("statusCode")) {
            ResponseIn in;
            try {
                in = ResponseInRW.INSTANCE.bytesReader().read(bytes);
            } catch (Exception e) {
                return new Outcome.Malformed("Invalid response: " + e.getMessage());
            }
            return in.statusCode() < 100 || in.statusCode() > 599
                ? new Outcome.Malformed("Invalid status code: " + in.statusCode())
                : new Outcome.Response(in);
        }
        return new Outcome.Response(new ResponseIn(
            200,
            Map.of("content-type", "application/json"),
            json instanceof String string ? string : new String(bytes, StandardCharsets.UTF_8),
            false,
            null
        ));
    }

    /// @param bytes     Posted error, a JSON object with `errorType` and `errorMessage`, or empty
    /// @param errorType Value of `Lambda-Runtime-Function-Error-Type`, may be null
    /// @return Outcome
    static Outcome error(byte[] bytes, String errorType) {
        Map<?, ?> error;
        try {
            error = bytes.length > 0 && Json.instance().read(bytes) instanceof Map<?, ?> map ? map : Map.of();
        } catch (Exception e) {
            error = Map.of();
        }
        var type = error.get("errorType") instanceof String string ? string
            : errorType != null ? errorType
                : "Unknown";
        var message = error.get("errorMessage") instanceof String string ? string : "";
        return new Outcome.FunctionError(type, message);
    }

    private LambdaResponses() {
    }
}
