import com.github.kjetilv.uplift.flambda.CorsSettings;
import com.github.kjetilv.uplift.flambda.LocalLambda;
import com.github.kjetilv.uplift.flambda.LocalLambdaSettings;
import com.github.kjetilv.uplift.flogs.Flogs;
import com.github.kjetilv.uplift.flogs.LogLevel;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static com.github.kjetilv.uplift.util.Time.UTC_CLOCK;

void main(String[] args) {
    Flogs.initialize(LogLevel.DEBUG);

    int lambdaPort = Arrays.stream(args)
        .map(Integer::parseInt)
        .findFirst().orElse(LAMBDA_PORT);

    int apiPort = Arrays.stream(args)
        .skip(1)
        .map(Integer::parseInt)
        .findFirst().orElse(API_PORT);

    var settings = new LocalLambdaSettings(
        lambdaPort,
        apiPort,
        REQUEST_BUFFER_SIZE,
        QUEUE_LENGTH,
        new CorsSettings(ORIGINS, METHODS, HEADERS),
        UTC_CLOCK::instant
    );
    try (
        var localLambda = new LocalLambda(settings)
    ) {
        var logger = LoggerFactory.getLogger("flambda");
        logger.info("Lambda: {}", localLambda);
        localLambda.run();
        logger.info("Done: {}", localLambda);
    }
}

private static final int LAMBDA_PORT = 8081;

private static final int API_PORT = 9001;

private static final int REQUEST_BUFFER_SIZE = 8 * 8192;

private static final int QUEUE_LENGTH = 10;

private static final List<String> METHODS = List.of("GET", "POST", "PUT", "DELETE");

private static final List<String> ORIGINS = List.of("*");

private static final List<String> HEADERS = List.of();
