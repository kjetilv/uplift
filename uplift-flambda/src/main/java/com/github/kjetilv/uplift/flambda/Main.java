package com.github.kjetilv.uplift.flambda;

import java.util.Arrays;
import java.util.List;

import com.github.kjetilv.uplift.flogs.Flogs;
import com.github.kjetilv.uplift.kernel.ManagedExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.kjetilv.uplift.kernel.ManagedExecutors.executor;
import static com.github.kjetilv.uplift.kernel.Time.UTC_CLOCK;

@SuppressWarnings("MagicNumber")
public final class Main {

    public static void main(String[] args) {
        Flogs.initialize(ManagedExecutors.threadNamer());
        Integer lambdaPort =
            Arrays.stream(args).map(Integer::parseInt).findFirst().orElse(8081);
        Integer apiPort =
            Arrays.stream(args).skip(1).map(Integer::parseInt).findFirst().orElse(9001);
        try (
            LocalLambda localLambda = new LocalLambda(
                new LocalLambdaSettings(
                    lambdaPort,
                    apiPort,
                    8 * 8192,
                    10,
                    new CorsSettings(
                        List.of("*"),
                        List.of("GET", "OPTIONS", "POST", "DELETE"),
                        List.of("content-type", "range")
                    ),
                    UTC_CLOCK::instant
                ),
                executor("L", 10),
                executor("S", 10)
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
