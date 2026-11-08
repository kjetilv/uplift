package com.github.kjetilv.uplift.flambda;

import com.github.kjetilv.uplift.lambda.ResponseIn;

/// How an API request ended
public sealed interface Outcome {

    /// The Lambda responded
    record Response(ResponseIn in) implements Outcome {
    }

    /// The Lambda reported an error for the invocation
    record FunctionError(String errorType, String errorMessage) implements Outcome {
    }

    /// The Lambda's response could not be used
    record Malformed(String reason) implements Outcome {
    }

    /// No response within the timeout
    record TimedOut() implements Outcome {
    }

    /// Too many requests waiting for a Lambda
    record Throttled() implements Outcome {
    }

    /// Flambda was closed
    record Closed() implements Outcome {
    }
}
