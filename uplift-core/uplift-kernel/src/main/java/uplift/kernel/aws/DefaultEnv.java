package uplift.kernel.aws;

import java.net.URI;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uplift.kernel.Env;

public final class DefaultEnv implements Env {

    private static final Logger log = LoggerFactory.getLogger(DefaultEnv.class);

    @Override
    @SuppressWarnings("HttpUrlsUsage")
    public URI awsLambdaUri() {
        return URI.create("http://" + awsLambdaRuntimeApi() + "/2018-06-01/runtime/invocation/next");
    }

    @Override
    public String awsLambdaRuntimeApi() {
        return getRequired("AWS_LAMBDA_RUNTIME_API");
    }

    @Override
    public String accessKey() {
        return get("aws.accessKeyId", "AWS_ACCESS_KEY_ID", true);
    }

    @Override
    public String secretKey() {
        return get("aws.secretAccessKey", "AWS_SECRET_ACCESS_KEY", true);
    }

    @Override
    public String sessionToken() {
        return get(null, "AWS_SESSION_TOKEN");
    }

    @Override
    public String authorizationToken() {
        return get(null, "AWS_CONTAINER_AUTHORIZATION_TOKEN");
    }

    @Override
    public String credentialsFullUri() {
        return get(null, "AWS_CONTAINER_CREDENTIALS_FULL_URI");
    }

    @Override
    public String s3Bucket() {
        return Optional.ofNullable(get("taninim.bucket", "TANINIM_BUCKET")).orElse("taninim-water");
    }

    private static Supplier<IllegalStateException> missing(
        String systemProperty, String environmentVariable
    ) {
        return () -> new IllegalStateException("Incomplete environment: " + systemProperty + "/" + environmentVariable);
    }

    @SuppressWarnings("SameParameterValue")
    private static String getRequired(String property) {
        return get(property, property, true);
    }

    private static String get(String systemProperty, String environmentVariable) {
        return get(systemProperty, environmentVariable, false);
    }

    private static String get(String systemProperty, String environmentVariable, boolean required) {
        Optional<String> value = systemProperty(systemProperty).or(() ->
            environmentVariable(environmentVariable));
        value.ifPresentOrElse(
            v -> log(systemProperty, environmentVariable, v),
            () -> logMissing(systemProperty, environmentVariable, required)
        );
        return required
            ? value.orElseThrow(missing(systemProperty, environmentVariable))
            : value.orElse(null);
    }

    private static void logMissing(String systemProperty, String environmentVariable, boolean required) {
        if (required) {
            log.debug("Missing: {}/{}", systemProperty, environmentVariable);
        } else {
            log.error("Missing: {}/{}", systemProperty, environmentVariable);
        }
    }

    private static Optional<String> systemProperty(String systemProperty) {
        return Optional.ofNullable(systemProperty).map(System::getProperty);
    }

    private static Optional<String> environmentVariable(String environmentVariable) {
        return Optional.ofNullable(environmentVariable).map(System::getenv);
    }

    private static void log(String systemProperty, String environmentVariable, String s) {
        int length = s.length();
        int section = Math.min(10, length / 3);
        log.debug(
            "{}/{} -> {}...{} ({} chars)",
            systemProperty,
            environmentVariable,
            s.substring(0, section),
            s.substring(length - section, length),
            length
        );
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[]";
    }
}
