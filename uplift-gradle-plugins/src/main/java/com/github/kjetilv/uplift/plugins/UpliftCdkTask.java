package com.github.kjetilv.uplift.plugins;

import com.github.kjetilv.uplift.plugins.core.CdkApp;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.work.DisableCachingByDefault;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/** Base for the tasks that generate the throwaway CDK application. */
@DisableCachingByDefault(because = "Deploying a stack is not cacheable")
public abstract class UpliftCdkTask extends UpliftTask {

    /** CDK brings its own copies of these, so they are not injected into the generated pom. */
    private static final Set<String> AWS = Set.of(
        "software.amazon.awscdk:aws-cdk-lib",
        "software.constructs:constructs"
    );

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract Property<Path> getStackbuilderJar();

    @Input
    public abstract Property<String> getStackbuilderClass();

    @Override
    public void stackWith(String name) {
        getStackbuilderClass().set(name);
    }

    protected final void initCdkApp() {
        new CdkApp(
            cdk(),
            getStackbuilderJar().getOrNull(),
            getStackbuilderClass().getOrElse(""),
            getAccount().get(),
            getRegion().get(),
            getStack().get(),
            dependencies()
        ).initialize();
    }

    private List<CdkApp.Dependency> dependencies() {
        Configuration compileClasspath = getProject().getConfigurations().findByName("compileClasspath");
        if (compileClasspath == null) {
            return List.of();
        }
        return compileClasspath.getIncoming().getDependencies().stream()
            .filter(dependency -> !isAws(dependency))
            .map(dependency -> new CdkApp.Dependency(
                dependency.getGroup(),
                dependency.getName(),
                dependency.getVersion()))
            .toList();
    }

    private static boolean isAws(Dependency dependency) {
        return AWS.contains(dependency.getGroup() + ":" + dependency.getName());
    }
}
