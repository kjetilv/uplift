import module java.base;
import module uplift.flambda;
import module uplift.flogs;

import org.slf4j.Logger;

import static com.github.kjetilv.uplift.util.Time.UTC_CLOCK;

@SuppressWarnings("MagicNumber")
void main(String[] args) {
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
            )
        )
    ) {
        Logger logger = LoggerFactory.getLogger("flambda");
        logger.info("Lambda: {}", localLambda);
        localLambda.run();
        logger.info("Done: {}", localLambda);
    }
}
