package com.github.kjetilv.uplift.flambda;

import java.time.Instant;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public record LocalLambdaSettings(
    Integer lambdaPort,
    Integer apiPort,
    int requestBufferSize,
    int queueLength,
    CorsSettings cors,
    Supplier<Instant> time
) {

    public LocalLambdaSettings(
        Integer lambdaPort,
        Integer apiPort,
        int requestBufferSize,
        int queueLength,
        CorsSettings cors,
        Supplier<Instant> time
    ) {
        this.lambdaPort = lambdaPort;
        this.apiPort = apiPort;
        this.requestBufferSize = Math.max(MIN_REQUEST_LENGTH, requestBufferSize);
        this.queueLength = Math.max(MIN_QUEUE_LENGTH, queueLength);
        this.cors = requireNonNull(cors, "cors");
        this.time = requireNonNull(time, "time");
    }

    private static final int MIN_REQUEST_LENGTH = 1_024;

    private static final int MIN_QUEUE_LENGTH = 1;
}
