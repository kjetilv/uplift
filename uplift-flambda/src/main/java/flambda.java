import module java.base;
import module uplift.flambda;
import module uplift.flogs;

import static com.github.kjetilv.uplift.util.Time.UTC_CLOCK;

@SuppressWarnings("MagicNumber")
void main(String[] args) {
    Flogs.initialize(LogLevel.DEBUG);
    int lambdaPort = Arrays.stream(args)
        .map(Integer::parseInt)
        .findFirst().orElse(8081);
    int apiPort = Arrays.stream(args).skip(1)
        .map(Integer::parseInt)
        .findFirst().orElse(9001);
    try (
        var localLambda = new LocalLambda(
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
        var logger = LoggerFactory.getLogger("flambda");
        logger.info("Lambda: {}", localLambda);
        localLambda.run();
        logger.info("Done: {}", localLambda);
    }
}
