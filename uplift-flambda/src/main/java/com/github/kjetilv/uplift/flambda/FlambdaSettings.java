package com.github.kjetilv.uplift.flambda;

import module java.base;
import com.github.kjetilv.uplift.synchttp.CorsSettings;
import com.github.kjetilv.uplift.util.Non;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

/// Settings for [Flambda].
///
/// @param name           Function name, used in the function ARN
/// @param address        Address to bind both servers to. Default loopback.
/// @param lambdaPort     Runtime API port. Null or 0 for any free port.
/// @param apiPort        API port. Null or 0 for any free port.
/// @param maxRequestSize Max API request body, in bytes. Larger requests get 413. Default 6 MiB, Lambda's limit.
/// @param queueLength    Max requests waiting for a Lambda. More get 429. Default 10.
/// @param timeout        Max time an API request waits for a Lambda response. Then 504. Default 1 minute.
/// @param cors           CORS settings. CORS headers from the Lambda are ignored.
/// @param time           Time for event timestamps and the invocation deadline. Default system time.
public record FlambdaSettings(
    String name,
    InetAddress address,
    Integer lambdaPort,
    Integer apiPort,
    int maxRequestSize,
    int queueLength,
    Duration timeout,
    CorsSettings cors,
    InstantSource time
) {

    private static final Logger log = LoggerFactory.getLogger(FlambdaSettings.class);

    /// Lambda's limit for synchronous invocation payloads
    public static final int DEFAULT_MAX_REQUEST_SIZE = 6 * 1024 * 1024;

    public static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(1);

    public FlambdaSettings(String name, CorsSettings cors) {
        this(name, cors, null);
    }

    public FlambdaSettings(String name, CorsSettings cors, InstantSource time) {
        this(name, 0, 0, cors, time);
    }

    public FlambdaSettings(
        String name,
        int maxRequestSize,
        int queueLength,
        CorsSettings cors,
        InstantSource time
    ) {
        this(
            name,
            null,
            null,
            null,
            maxRequestSize,
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
        int maxRequestSize,
        int queueLength,
        CorsSettings cors,
        InstantSource time
    ) {
        this(
            name,
            null,
            lambdaPort,
            apiPort,
            maxRequestSize,
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
        int maxRequestSize,
        int queueLength,
        Duration timeout,
        CorsSettings cors,
        InstantSource time
    ) {
        this.name = requireNonNull(name, "name");
        this.address = address == null
            ? InetAddress.getLoopbackAddress()
            : address;
        this.lambdaPort = lambdaPort;
        this.apiPort = apiPort;
        this.maxRequestSize = Non.negative(maxRequestSize, "maxRequestSize") == 0
            ? DEFAULT_MAX_REQUEST_SIZE
            : maxRequestSize;
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

    private static final int DEFAULT_QUEUE_LENGTH = 10;

    private static Object print(Integer port) {
        return port == null || port == 0 ? "<?>" : port;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + ": " + address + ":" +
               print(apiPort) + "->" +
               print(lambdaPort) + "]";
    }
}
