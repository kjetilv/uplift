package com.github.kjetilv.uplift.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.net.URI;
import java.nio.file.Path;

public class NativeLambdaPlugin implements Plugin<Project> {

    private static final String GRAAL_DIST =
        "https://github.com/graalvm/graalvm-ce-builds/releases/download/graal-25.2.4/"
        + "graalvm-community-jdk-25i2-25.0.4_linux-aarch64_bin.tar.gz";

    @Override
    public void apply(Project project) {
        project.getTasks().register("native-lambda", NativeLambdaTask.class, task -> {
            String name = project.getName();
            Path target = Projects.buildSubDirectory(project, "uplift");

            task.getClassPath().set(Projects.classpath(project));
            task.getZipFile().set(target.resolve(name + ".zip"));
            task.getIdentifier().set(name);
            task.getBootstrapFile().set(target.resolve(name));
            task.getArch().set(System.getProperty("os.arch"));
            task.getBuildsite().set(Projects.shortGroupName(project) + "-buildsite");
            task.getJavaDist().set(URI.create(GRAAL_DIST));

            task.getEnablePreview().convention(false);
            task.getAddModules().convention("");
            task.getOtherOptions().convention("");

            task.getLogger().info(
                task + ": Classpath: " + Projects.classpath(project)
                + ", Zipfile: " + target.resolve(name + ".zip"));

            task.dependsOn("jar");
        });
    }
}
