package com.github.kjetilv.uplift.flambda;

import module java.base;

import com.github.kjetilv.uplift.synchttp.CorsSettings;
import com.github.kjetilv.uplift.util.Non;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public record FlambdaSettings(
    String name,
    InetAddress address,
    Integer lambdaPort,
    Integer apiPort,
    int requestBufferSize,
    int queueLength,
    Duration timeout,
    CorsSettings cors,
    Supplier<Instant> time
) {

    private static final Logger log = LoggerFactory.getLogger(FlambdaSettings.class);

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
            null,
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
            null,
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
        Duration timeout,
        CorsSettings cors,
        Supplier<Instant> time
    ) {
        this.name = requireNonNull(name, "name");
        this.address = address == null
            ? InetAddress.getLoopbackAddress()
            : address;
        this.lambdaPort = lambdaPort;
        this.apiPort = apiPort;
        this.requestBufferSize = Non.negative(requestBufferSize, "requestBufferSize") == 0
            ? DEFAULT_REQUEST_LENGTH
            : requestBufferSize;
        this.queueLength = Non.negative(queueLength, "queueLength") == 0
            ? DEFAULT_QUEUE_LENGTH
            : queueLength;
        this.timeout = timeout != null && timeout.isPositive()
            ? timeout
            : DEFAULT_TIMEOUT;
        this.cors = requireNonNull(cors, "cors");
        this.time = time == null ? Instant::now : time;
        log.info("{} created", this);
    }

    private Object print(Integer port) {
        return port == null || port == 0 ? "<?>" : port;
    }

    private static final int DEFAULT_REQUEST_LENGTH = 1_024;

    private static final int DEFAULT_QUEUE_LENGTH = 10;

    public static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(1);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + ": " + address + ":" +
               print(apiPort) + "->" +
               print(lambdaPort) + "]";
    }
}
