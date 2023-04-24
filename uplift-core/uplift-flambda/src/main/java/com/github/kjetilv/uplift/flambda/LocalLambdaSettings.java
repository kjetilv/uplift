package com.github.kjetilv.uplift.flambda;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public record LocalLambdaSettings(
    int lambdaPort,
    int apiPort,
    int requestBufferSize,
    int queueLength,
    ExecutorService lambdaExecutor,
    ExecutorService serverExecutor,
    CorsSettings corsSettings,
    Supplier<Instant> time
) {

    public LocalLambdaSettings(
        int lambdaPort,
        int apiPort,
        int requestBufferSize,
        int queueLength,
        ExecutorService lambdaExecutor,
        ExecutorService serverExecutor,
        CorsSettings corsSettings,
        Supplier<Instant> time
    ) {
        this.lambdaPort = lambdaPort;
        this.apiPort = apiPort;
        this.requestBufferSize = Math.max(MIN_REQUEST_LENGTH, requestBufferSize);
        this.queueLength = Math.max(MIN_QUEUE_LENGTH, queueLength);
        this.lambdaExecutor = requireNonNull(lambdaExecutor, "lambdaExecutor");
        this.serverExecutor = requireNonNull(serverExecutor, "serverExecutor");
        this.corsSettings = requireNonNull(corsSettings, "corsSettings");
        this.time = requireNonNull(time, "time");
    }

    private static final int MIN_REQUEST_LENGTH = 1_024;

    private static final int MIN_QUEUE_LENGTH = 1;
}
