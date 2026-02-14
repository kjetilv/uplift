package com.github.kjetilv.uplift.flambda;

import module java.base;

import com.github.kjetilv.uplift.synchttp.CorsSettings;

import static java.util.Objects.requireNonNull;

public record FlambdaSettings(
    String name,
    InetAddress address,
    Integer lambdaPort,
    Integer apiPort,
    int requestBufferSize,
    int queueLength,
    CorsSettings cors,
    Supplier<Instant> time
) {

    public FlambdaSettings(String name, CorsSettings cors) {
        this(name, cors, null);
    }

    public FlambdaSettings(String name, CorsSettings cors, Supplier<Instant> time) {
        this(name, 0, 0, cors, time);
    }

    public FlambdaSettings(
        String name,
        int requestBufferSize,
        int queueLength,
        CorsSettings cors,
        Supplier<Instant> time
    ) {
        this(
            name,
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
        String name,
        Integer lambdaPort,
        Integer apiPort,
        int requestBufferSize,
        int queueLength,
        CorsSettings cors,
        Supplier<Instant> time
    ) {
        this(
            name,
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
        String name,
        InetAddress address,
        Integer lambdaPort,
        Integer apiPort,
        int requestBufferSize,
        int queueLength,
        CorsSettings cors,
        Supplier<Instant> time
    ) {
        this.name = requireNonNull(name, "name");
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + " " + address + ":" + apiPort + "->" + lambdaPort + "]";
    }
}
