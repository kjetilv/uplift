package com.github.kjetilv.uplift.flambda;

import com.github.kjetilv.uplift.flogs.Flogs;
import com.github.kjetilv.uplift.flogs.LogLevel;
import com.github.kjetilv.uplift.kernel.ManagedExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static com.github.kjetilv.uplift.kernel.ManagedExecutors.executor;
import static com.github.kjetilv.uplift.kernel.Time.UTC_CLOCK;

@SuppressWarnings("MagicNumber")
public final class Main {

    public static void main(String[] args) {
        ManagedExecutors.configure(4, 10);
        Flogs.initialize(LogLevel.DEBUG);
        Integer lambdaPort =
            Arrays.stream(args)
                .map(Integer::parseInt)
                .findFirst().orElse(8081);
        Integer apiPort =
            Arrays.stream(args).skip(1)
                .map(Integer::parseInt)
                .findFirst().orElse(9001);
        try (
            LocalLambda localLambda = new LocalLambda(
                new LocalLambdaSettings(
                    lambdaPort,
                    apiPort,
                    8 * 8192,
                    10,
                    new CorsSettings(
                        List.of("*"),
                        List.of("GET", "POST", "PUT", "DELETE"),
                        List.of()
                    ),
                    UTC_CLOCK::instant
                ),
                executor("L", 4),
                executor("S", 4)
            )
        ) {
            logger().info("Lambda: {}", localLambda);
            localLambda.join();
            logger().info("Done: {}", localLambda);
        }
    }

    static Logger logger() {
        return LoggerFactory.getLogger(Main.class);
    }

    private Main() {
    }
}
