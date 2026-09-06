package com.github.kjetilv.uplift.plugins.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates the throwaway CDK application: `cdk init` in the container, the stack builder
 * jar alongside it, a generated main class, and a pom rewritten to launch that main with
 * the uplift coordinates as system properties.
 */
public final class CdkApp {

    private static final String CLOUD_APP = "CloudApp.java";

    private static final String APP_PACKAGE = "src/main/java/lambda/uplift/app";

    private static final String GENERATED_MAIN = "lambda.uplift.app.CloudApp";

    private static final String TEMPLATE_MAIN = "com.myorg.AppApp";

    private final Cdk cdk;

    private final Path stackbuilderJar;

    private final String stackbuilderClass;

    private final String account;

    private final String region;

    private final String stack;

    private final List<Dependency> dependencies;

    /** A dependency to inject into the generated pom. */
    public record Dependency(String group, String name, String version) {
    }

    public CdkApp(
        Cdk cdk,
        Path stackbuilderJar,
        String stackbuilderClass,
        String account,
        String region,
        String stack,
        List<Dependency> dependencies
    ) {
        this.cdk = cdk;
        this.stackbuilderJar = stackbuilderJar;
        this.stackbuilderClass = stackbuilderClass;
        this.account = account;
        this.region = region;
        this.stack = stack;
        this.dependencies = List.copyOf(dependencies);
    }

    public void initialize() {
        Path app = cdk.cdkApp();
        FileIO.clearRecursive(app);
        cdk.run("cdk init --language=java --generate-only");
        FileIO.copyTo(resolvedJar(), app);
        try {
            Path sourcePackage = app.resolve(APP_PACKAGE);
            Files.createDirectories(sourcePackage);
            Files.write(sourcePackage.resolve(CLOUD_APP), Templates.loadResourceLines(CLOUD_APP));
            FileIO.clearRecursive(
                app.resolve("src/main/java/com"),
                app.resolve("src/test/java/com")
            );
            Path pom = app.resolve("pom.xml");
            Path copy = Files.copy(pom, app.resolve("pom.xml.orig"));
            Files.write(pom, templated(copy));
            FileIO.clearRecursive(copy);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate CDK app in " + app, e);
        }
    }

    private List<String> templated(Path pom) throws IOException {
        List<String> result = new ArrayList<>();
        for (String line : Files.readAllLines(pom, StandardCharsets.UTF_8)) {
            if (line.contains("<mainClass>")) {
                result.addAll(addedMain(indent(line), line));
            } else if (line.contains("</dependencies>")) {
                result.addAll(addedDependencies(indent(line), line));
            } else {
                result.add(line);
            }
        }
        return result;
    }

    private List<String> addedMain(String indent, String line) {
        List<String> lines = new ArrayList<>();
        lines.add(line.replace(TEMPLATE_MAIN, GENERATED_MAIN));
        lines.add(indent + "<systemProperties>");
        properties().forEach((key, value) ->
            lines.add(indent + "    <systemProperty><key>" + key + "</key><value>" + value + "</value></systemProperty>"));
        lines.add(indent + "</systemProperties>");
        return lines;
    }

    private Map<String, String> properties() {
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("uplift.account", account);
        properties.put("uplift.region", region);
        properties.put("uplift.stack", stack);
        properties.put("uplift.stackbuilderJar", resolvedJar().getFileName().toString());
        properties.put("uplift.stackbuilderClass", stackbuilderClass == null ? "" : stackbuilderClass);
        return properties;
    }

    private List<String> addedDependencies(String indent, String line) {
        List<String> lines = new ArrayList<>();
        dependencies.forEach(dependency -> {
            lines.add("");
            lines.add(indent + "    <dependency>");
            lines.add(indent + "      <groupId>" + dependency.group() + "</groupId>");
            lines.add(indent + "      <artifactId>" + dependency.name() + "</artifactId>");
            lines.add(indent + "      <version>" + dependency.version() + "</version>");
            lines.add(indent + "    </dependency>");
        });
        lines.add(line);
        return lines;
    }

    private Path resolvedJar() {
        if (stackbuilderJar == null) {
            throw new IllegalStateException("Required a stackbuilderJar");
        }
        return stackbuilderJar;
    }

    private static String indent(String line) {
        int end = 0;
        while (end < line.length() && Character.isWhitespace(line.charAt(end))) {
            end++;
        }
        return line.substring(0, end);
    }
}
