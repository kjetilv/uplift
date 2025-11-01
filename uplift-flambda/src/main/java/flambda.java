import com.github.kjetilv.uplift.flambda.CorsSettings;
import com.github.kjetilv.uplift.flambda.LocalLambda;
import com.github.kjetilv.uplift.flambda.LocalLambdaSettings;
import com.github.kjetilv.uplift.flogs.Flogs;
import com.github.kjetilv.uplift.flogs.LogLevel;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

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
