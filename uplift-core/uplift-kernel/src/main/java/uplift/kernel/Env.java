package uplift.kernel;

import java.net.URI;

import uplift.kernel.aws.DefaultEnv;

public interface Env {

    static Env actual() {
        return new DefaultEnv();
    }

    URI awsLambdaUri();

    String awsLambdaRuntimeApi();

    String accessKey();

    String secretKey();

    String sessionToken();

    String authorizationToken();

    String credentialsFullUri();

    String s3Bucket();
}
