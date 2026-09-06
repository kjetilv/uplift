package com.github.kjetilv.uplift.plugins.maven;

import com.github.kjetilv.uplift.plugins.core.Cdk;
import com.github.kjetilv.uplift.plugins.core.Docker;
import com.github.kjetilv.uplift.plugins.core.Log;
import com.github.kjetilv.uplift.plugins.core.StackReport;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shared coordinates and plumbing for the goals that drive the CDK container.
 * <p>
 * The Gradle plugin read these from project properties. Here they are ordinary parameters,
 * so they can come from the pom, from {@code .mvn/maven.config}, or from the command line.
 */
public abstract class AbstractUpliftMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(property = "uplift.account", required = true)
    protected String account;

    @Parameter(property = "uplift.region", required = true)
    protected String region;

    @Parameter(property = "uplift.profile", defaultValue = "default")
    protected String profile;

    /** Defaults to the module's coordinates, normalised, as the Gradle plugin did. */
    @Parameter(property = "uplift.stack")
    protected String stack;

    @Parameter(property = "uplift.env")
    protected Map<String, String> env;

    @Parameter(property = "docker.binary", defaultValue = Docker.DEFAULT_BINARY)
    protected String dockerBinary;

    @Override
    public void execute() throws MojoExecutionException {
        selfCheck();
        try {
            cdk().initialize();
            perform();
        } catch (MojoExecutionException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new MojoExecutionException(getClass().getSimpleName() + " failed", e);
        }
    }

    protected abstract void perform() throws MojoExecutionException;

    protected final Cdk cdk() {
        return new Cdk(upliftDir(), cdkApp(), awsAuth(), architecture(), env(), docker(), log());
    }

    protected final Docker docker() {
        return new Docker(dockerBinary, log());
    }

    protected final Log log() {
        return new MavenLog(getLog());
    }

    protected final void report(List<Path> sources) {
        new StackReport(stack(), region, profile, log()).report(sources);
    }

    protected final String profileOption() {
        return profile == null || profile.isBlank() ? "" : "--profile=" + profile;
    }

    protected final Path buildSubDirectory(String name) {
        Path path = Path.of(project.getBuild().getDirectory()).resolve(name);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create " + path, e);
        }
        return path;
    }

    protected final Path upliftDir() {
        return buildSubDirectory("uplift");
    }

    protected final Path cdkApp() {
        return buildSubDirectory("cdk-app");
    }

    protected final String stack() {
        return stack != null && !stack.isBlank() ? stack : composedName();
    }

    private Map<String, String> env() {
        return env == null ? Map.of() : env;
    }

    private String composedName() {
        String group = project.getGroupId();
        String name = project.getArtifactId().startsWith(group)
            ? project.getArtifactId()
            : group + "-" + project.getArtifactId();
        StringBuilder normalized = new StringBuilder(name.length());
        name.chars().forEach(character ->
            normalized.append(Character.isLetterOrDigit(character) ? (char) character : '-'));
        return normalized.toString();
    }

    /** The CDK container image is tagged per architecture, using docker's naming. */
    private static String architecture() {
        String arch = System.getProperty("os.arch");
        return switch (arch) {
            case "aarch64" -> "arm64v8";
            case "x86_64" -> "amd64";
            default -> arch;
        };
    }

    private static String awsAuth() {
        return Path.of(System.getProperty("user.home")).resolve(".aws").toAbsolutePath().toString();
    }

    private void selfCheck() throws MojoExecutionException {
        List<String> missing = new ArrayList<>();
        if (account == null || account.isBlank()) {
            missing.add("account");
        }
        if (region == null || region.isBlank()) {
            missing.add("region");
        }
        if (profile == null || profile.isBlank()) {
            missing.add("profile");
        }
        if (!missing.isEmpty()) {
            throw new MojoExecutionException(
                "Missing config! Set these in the pom, in .mvn/maven.config, or on the command "
                + "line as -Duplift.<name>:\n " + String.join("\n ", missing));
        }
    }
}
