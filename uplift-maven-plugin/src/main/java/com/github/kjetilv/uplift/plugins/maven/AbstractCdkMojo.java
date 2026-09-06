package com.github.kjetilv.uplift.plugins.maven;

import com.github.kjetilv.uplift.plugins.core.CdkApp;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/** Base for the goals that generate the throwaway CDK application. */
public abstract class AbstractCdkMojo extends AbstractUpliftMojo {

    /** CDK brings its own copies of these, so they are not injected into the generated pom. */
    private static final Set<String> AWS = Set.of(
        "software.amazon.awscdk:aws-cdk-lib",
        "software.constructs:constructs"
    );

    /** Defaults to the jar this module builds. */
    @Parameter(property = "uplift.stackbuilderJar")
    protected File stackbuilderJar;

    /** Empty means "take the first Consumer&lt;Stack&gt; found in the jar". */
    @Parameter(property = "uplift.stackbuilderClass", defaultValue = "")
    protected String stackbuilderClass;

    protected final void initCdkApp() throws MojoExecutionException {
        new CdkApp(
            cdk(),
            resolvedStackbuilderJar(),
            stackbuilderClass == null ? "" : stackbuilderClass,
            account,
            region,
            stack(),
            declaredDependencies()
        ).initialize();
    }

    /**
     * The jar holding the stack builder.
     * <p>
     * When these goals are invoked directly, rather than bound to a phase, the package
     * phase has not run and the project artifact has no file yet. The built jar is then
     * looked up where package would have put it, so that both `mvn package uplift:init`
     * and a bare `mvn uplift:init` after an earlier build work.
     */
    private Path resolvedStackbuilderJar() throws MojoExecutionException {
        if (stackbuilderJar != null) {
            return stackbuilderJar.toPath();
        }
        File attached = project.getArtifact() == null ? null : project.getArtifact().getFile();
        if (attached != null) {
            return attached.toPath();
        }
        Path built = Path.of(project.getBuild().getDirectory())
            .resolve(project.getBuild().getFinalName() + ".jar");
        if (!Files.isRegularFile(built)) {
            throw new MojoExecutionException(
                "No jar to read the stack builder from. Expected " + built
                + ". Run `mvn package` first, or bind this goal to a phase after package.");
        }
        return built;
    }

    /**
     * Only jars belong on the CDK app's classpath. The lambda zips are declared as
     * dependencies purely to move them between modules, and the container has no
     * repository to resolve them from anyway.
     */
    private static boolean isJar(Dependency dependency) {
        String type = dependency.getType();
        return type == null || type.isBlank() || "jar".equals(type);
    }

    /**
     * Declared dependencies, not resolved ones. The Gradle version read the declared
     * dependencies of compileClasspath, so using the resolved set here would inject the
     * whole transitive tree into the generated CDK pom.
     */
    private List<CdkApp.Dependency> declaredDependencies() {
        return project.getDependencies().stream()
            .filter(dependency -> !"test".equals(dependency.getScope()))
            .filter(AbstractCdkMojo::isJar)
            .filter(dependency -> !AWS.contains(dependency.getGroupId() + ":" + dependency.getArtifactId()))
            .map(dependency -> new CdkApp.Dependency(
                dependency.getGroupId(),
                dependency.getArtifactId(),
                dependency.getVersion()))
            .toList();
    }
}
