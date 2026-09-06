package com.github.kjetilv.uplift.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class UpliftPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getTasks().register("uplift-init", UpliftInitTask.class, task -> {
            configureFor(project, task);
            Path jar = task.getStackbuilderJar().getOrNull();
            if (jar != null) {
                task.getActiveJar().set(Projects.cdkFile(project, jar.getFileName().toString()));
            }
            task.getActivePom().set(Projects.cdkFile(project, "pom.xml"));
        });
        project.getTasks().register("uplift-bootstrap", UpliftBootstrapTask.class, task -> {
            configureFor(project, task);
            task.getTemplate().set(templateFile(project, task.getStack().get()));
            task.dependsOn("uplift-init");
        });
        project.getTasks().register("uplift", UpliftDeployTask.class, task -> {
            configureFor(project, task);
            task.dependsOn("uplift-bootstrap");
        });
        project.getTasks().register("uplift-ping", UpliftPingTask.class, task ->
            configureFor(project, task));
        project.getTasks().register("uplift-destroy", UpliftDestroyTask.class, task ->
            configureFor(project, task));
    }

    private static void configureFor(Project project, UpliftTask task) {
        task.getAccount().set(Projects.propertyOr(project, "account"));
        task.getRegion().set(Projects.propertyOr(project, "region"));
        task.getProfile().set(Projects.propertyOr(project, "profile", () -> "default"));
        task.getStack().set(Projects.propertyOr(project, "stack", () -> composeName(project)));
        task.getArch().set(architecture());
        task.getAwsAuth().set(awsAuth());
        if (task instanceof UpliftCdkTask cdkTask) {
            jarOutput(project).ifPresent(cdkTask.getStackbuilderJar()::set);
            cdkTask.getStackbuilderClass().set("");
        }
    }

    private static Path templateFile(Project project, String stack) {
        return Projects.cdkApp(project).resolve("cdk.out").resolve(stack + ".template.json");
    }

    /** The single jar this project builds, if it builds exactly one. */
    private static Optional<Path> jarOutput(Project project) {
        Task jar = project.getTasks().findByName("jar");
        if (jar == null) {
            return Optional.empty();
        }
        List<File> found = List.copyOf(jar.getOutputs().getFiles().getFiles());
        return found.size() == 1 ? Optional.of(found.getFirst().toPath()) : Optional.empty();
    }

    private static String composeName(Project project) {
        String group = String.valueOf(project.getGroup());
        String name = project.getName().startsWith(group)
            ? project.getName()
            : group + "-" + project.getName();
        return normalize(name);
    }

    private static String normalize(String name) {
        StringBuilder normalized = new StringBuilder(name.length());
        name.chars().forEach(character ->
            normalized.append(Character.isLetterOrDigit(character) ? (char) character : '-'));
        return normalized.toString();
    }

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
}
