package com.github.kjetilv.uplift.plugins.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builds and drives the `cdk-site` container. The image carries the CDK CLI and maven, so
 * that neither is needed on the host.
 */
public final class Cdk {

    public static final String IMAGE = "cdk-site:latest";

    private static final String TEMPLATE = "cdk-st4/Dockerfile";

    private final Path upliftDir;

    private final Path cdkApp;

    private final String awsAuth;

    private final String arch;

    private final Map<String, String> env;

    private final Docker docker;

    private final Log log;

    public Cdk(
        Path upliftDir,
        Path cdkApp,
        String awsAuth,
        String arch,
        Map<String, String> env,
        Docker docker,
        Log log
    ) {
        this.upliftDir = upliftDir;
        this.cdkApp = cdkApp;
        this.awsAuth = awsAuth;
        this.arch = arch;
        this.env = new LinkedHashMap<>(env == null ? Map.of() : env);
        this.docker = docker;
        this.log = log;
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
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write CDK Dockerfile in " + upliftDir, e);
        }
        docker.run(upliftDir, "build --tag " + IMAGE + " " + upliftDir);
    }

    public void run(String command) {
        String volumes = padRight(volumes());
        String environment = padRight(environment());
        docker.run(upliftDir, "run " + volumes + environment + IMAGE + " " + command);
    }

    private String volumes() {
        return "-v " + awsAuth + ":/root/.aws"
               + " -v " + cdkApp + ":/opt/app"
               + " -v " + upliftDir + ":/lambdas";
    }

    private String environment() {
        return env.entrySet().stream()
            .map(entry -> "-e " + entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining(" "));
    }

    private static String padRight(String value) {
        if (value == null || value.isBlank() || value.endsWith(" ")) {
            return value == null ? "" : value;
        }
        return value + " ";
    }
}
