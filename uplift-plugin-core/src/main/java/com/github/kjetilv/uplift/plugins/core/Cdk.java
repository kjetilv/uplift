package com.github.kjetilv.uplift.plugins.core;

import module java.base;

/**
 * Builds and drives the `cdk-site` container. The image carries the CDK CLI and maven, so
 * that neither is needed on the host.
 */
public final class Cdk {

    public static final String IMAGE = "cdk-site:latest";

    private final Path upliftDir;

    private final Path cdkApp;

    private final String awsAuth;

    private final String arch;

    private final Map<String, String> env;

    private final Docker docker;

    public Cdk(
        Path upliftDir,
        Path cdkApp,
        String awsAuth,
        String arch,
        Map<String, String> env,
        Docker docker
    ) {
        this.upliftDir = upliftDir;
        this.cdkApp = cdkApp;
        this.awsAuth = awsAuth;
        this.arch = arch;
        this.env = env == null ? Map.of() : Map.copyOf(env);
        this.docker = docker;
    }

    public Path upliftDir() {
        return upliftDir;
    }

    public Path cdkApp() {
        return cdkApp;
    }

    /** Renders the container Dockerfile and builds the image. Run before any CDK command. */
    public void initialize() {
        try {
            Files.write(
                upliftDir.resolve("Dockerfile"),
                Templates.renderResource(TEMPLATE, Map.of("arch", arch))
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write CDK Dockerfile in " + upliftDir, e);
        }
        docker.run(upliftDir, "build --tag " + IMAGE + " " + upliftDir);
    }

    public void run(String command) {
        var dockerCommand = String.join(" ", "run", volumes(), environment(), IMAGE, command);
        docker.run(upliftDir, dockerCommand);
    }

    private String volumes() {
        return "-v " + awsAuth + ":/root/.aws"
               + " -v " + cdkApp + ":/opt/app"
               + " -v " + upliftDir + ":/lambdas";
    }

    private String environment() {
        return env.entrySet()
            .stream()
            .map(entry -> "-e " + entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining(" "));
    }

    private static final String TEMPLATE = "cdk/Dockerfile";

    private static String padRight(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        if (value.endsWith(" ")) {
            return value;
        }
        return value + " ";
    }
}
