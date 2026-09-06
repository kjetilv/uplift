package com.github.kjetilv.uplift.plugins.maven;

import com.github.kjetilv.uplift.plugins.core.CdkApp;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
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

    protected final void initCdkApp() {
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

    private Path resolvedStackbuilderJar() {
        if (stackbuilderJar != null) {
            return stackbuilderJar.toPath();
        }
        File own = project.getArtifact() == null ? null : project.getArtifact().getFile();
        return own == null ? null : own.toPath();
    }

    /**
     * Declared dependencies, not resolved ones. The Gradle version read the declared
     * dependencies of compileClasspath, so using the resolved set here would inject the
     * whole transitive tree into the generated CDK pom.
     */
    private List<CdkApp.Dependency> declaredDependencies() {
        return project.getDependencies().stream()
            .filter(dependency -> !"test".equals(dependency.getScope()))
            .filter(dependency -> !AWS.contains(dependency.getGroupId() + ":" + dependency.getArtifactId()))
            .map(dependency -> new CdkApp.Dependency(
                dependency.getGroupId(),
                dependency.getArtifactId(),
                dependency.getVersion()))
            .toList();
    }
}
