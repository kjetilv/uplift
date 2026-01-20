package com.github.kjetilv.uplift.flambda;

import module java.base;

import static java.util.Objects.requireNonNull;

public record FlambdaSettings(
    InetAddress address,
    Integer lambdaPort,
    Integer apiPort,
    int requestBufferSize,
    int queueLength,
    CorsSettings cors,
    Supplier<Instant> time
) {

    public FlambdaSettings(CorsSettings cors) {
        this(cors, null);
    }

    public FlambdaSettings(CorsSettings cors, Supplier<Instant> time) {
        this(0, 0, cors, time);
    }

    public FlambdaSettings(
        int requestBufferSize,
        int queueLength,
        CorsSettings cors,
        Supplier<Instant> time
    ) {
        this(
            null,
            null,
            null,
            requestBufferSize,
            queueLength,
            cors,
            time
        );
    }

    public FlambdaSettings(
        Integer lambdaPort,
        Integer apiPort,
        int requestBufferSize,
        int queueLength,
        CorsSettings cors,
        Supplier<Instant> time
    ) {
        this(
            null,
            lambdaPort,
            apiPort,
            requestBufferSize,
            queueLength,
            cors,
            time
        );
    }

    public FlambdaSettings(
        InetAddress address,
        Integer lambdaPort,
        Integer apiPort,
        int requestBufferSize,
        int queueLength,
        CorsSettings cors,
        Supplier<Instant> time
    ) {
        this.address = address == null ? InetAddress.getLoopbackAddress() : address;
        this.lambdaPort = lambdaPort;
        this.apiPort = apiPort;
        this.requestBufferSize = Math.max(MIN_REQUEST_LENGTH, requestBufferSize);
        this.queueLength = Math.max(MIN_QUEUE_LENGTH, queueLength);
        this.cors = requireNonNull(cors, "cors");
        this.time = time == null ? Instant::now : requireNonNull(time, "time");
    }

    private static final int MIN_REQUEST_LENGTH = 1_024;

    private static final int MIN_QUEUE_LENGTH = 1;
}
