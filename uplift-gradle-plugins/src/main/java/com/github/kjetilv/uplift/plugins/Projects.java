package com.github.kjetilv.uplift.plugins;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/** Gradle side lookups. Everything build system specific lives here or in the tasks. */
final class Projects {

    private static final String RUNTIME_CLASSPATH = "runtimeClasspath";

    static String propertyOr(Project project, String name, Supplier<String> defaultValue) {
        return project.hasProperty(name)
            ? String.valueOf(project.property(name))
            : defaultValue.get();
    }

    static String propertyOr(Project project, String name) {
        return propertyOr(project, name, () -> "");
    }

    /** System property, then environment variable, then project property, then default. */
    static String resolveProperty(Project project, String name, String variable, String defaultValue) {
        String system = System.getProperty(name);
        if (system != null) {
            return system;
        }
        String environment = variable == null ? null : System.getenv(variable);
        if (environment != null) {
            return environment;
        }
        return project.hasProperty(name) ? String.valueOf(project.property(name)) : defaultValue;
    }

    static Path buildSubDirectory(Project project, String directory) {
        Path path = project.getLayout().getBuildDirectory().dir(directory).get().getAsFile().toPath();
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create " + path, e);
        }
        return path;
    }

    static Path cdkApp(Project project) {
        return buildSubDirectory(project, "cdk-app");
    }

    static Path cdkFile(Project project, String name) {
        return cdkApp(project).resolve(name);
    }

    /**
     * Resolved runtime classpath, in resolution order. Order reaches the generated
     * Dockerfile's `--class-path`, so it must be preserved.
     */
    static List<File> classpath(Project project) {
        Optional<Configuration> runtime = project.getConfigurations().stream()
            .filter(configuration -> RUNTIME_CLASSPATH.equalsIgnoreCase(configuration.getName()))
            .findFirst();
        if (runtime.isEmpty()) {
            project.getLogger().warn(
                "WARNING: No configuration found: " + project.getPath() + ", candidates: "
                + project.getConfigurations().stream().map(Configuration::getName).toList());
            return List.of();
        }
        List<File> files = new ArrayList<>();
        runtime.get().forEach(files::add);
        return files;
    }

    static String shortGroupName(Project project) {
        String base = base(project);
        int dot = base.lastIndexOf('.');
        String name = dot > 0 ? base.substring(dot + 1) : base;
        if (name.isBlank()) {
            throw new IllegalStateException("Blank shortname");
        }
        return name;
    }

    /** Names of the tasks this task depends on, as declared. */
    static List<String> dependencyTasks(Task task) {
        return task.getDependsOn().stream().map(String::valueOf).toList();
    }

    /** Output files of the tasks this task depends on. */
    static List<Path> dependencyOutputs(Task task) {
        List<Path> outputs = new ArrayList<>();
        dependencyTasks(task).forEach(name -> {
            Task found = task.getProject().getTasks().findByPath(name);
            if (found != null) {
                found.getOutputs().getFiles().getFiles().forEach(file -> outputs.add(file.toPath()));
            }
        });
        return outputs;
    }

    private static String base(Project project) {
        String group = String.valueOf(project.getGroup());
        if (!group.isBlank()) {
            return group;
        }
        return Path.of(System.getProperty("user.dir")).toAbsolutePath().getFileName().toString();
    }

    private Projects() {
    }
}
