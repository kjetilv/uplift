package uplift.flambda;

import java.util.Arrays;
import java.util.List;

import uplift.flogs.Flogs;
import uplift.kernel.ManagedExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uplift.kernel.ManagedExecutors.executor;
import static uplift.kernel.Time.UTC_CLOCK;

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
                    executor("L", 10),
                    executor("S", 10),
                    new CorsSettings(
                        List.of("*"),
                        List.of("GET", "OPTIONS", "POST", "DELETE"),
                        List.of("content-type", "range")
                    ),
                    UTC_CLOCK::instant
                ))
        ) {
            logger().info("Lambda: {}", localLambda);
            localLambda.join();
            logger().info("Done: {}", localLambda);
        }
    }

    static Logger logger() {
        return LoggerFactory.getLogger(Main.class);
    }
}
