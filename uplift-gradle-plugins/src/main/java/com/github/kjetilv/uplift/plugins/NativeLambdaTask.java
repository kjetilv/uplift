package com.github.kjetilv.uplift.plugins;

import com.github.kjetilv.uplift.plugins.core.Docker;
import com.github.kjetilv.uplift.plugins.core.FileIO;
import com.github.kjetilv.uplift.plugins.core.NativeImage;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Thin wrapper over {@link NativeImage}. */
@CacheableTask
public abstract class NativeLambdaTask extends DefaultTask {

    public NativeLambdaTask() {
        setGroup("uplift");
    }

    @Input
    public abstract Property<String> getIdentifier();

    @Input
    public abstract Property<String> getArch();

    @Input
    public abstract Property<String> getMain();

    @Input
    public abstract Property<URI> getJavaDist();

    @Input
    public abstract Property<String> getBuildsite();

    @OutputFile
    public abstract Property<Path> getZipFile();

    @OutputFile
    public abstract Property<Path> getBootstrapFile();

    @Classpath
    public abstract ListProperty<File> getClassPath();

    @Input
    public abstract Property<Boolean> getEnablePreview();

    @Input
    public abstract Property<String> getAddModules();

    @Input
    public abstract Property<String> getOtherOptions();

    @TaskAction
    public void perform() {
        NativeImage.Spec spec = new NativeImage.Spec(
            getIdentifier().get(),
            getArch().get(),
            getMain().get(),
            getJavaDist().get(),
            getBuildsite().get(),
            getZipFile().get(),
            classPath(),
            getEnablePreview().getOrElse(false),
            getAddModules().getOrElse(""),
            getOtherOptions().getOrElse("")
        );
        new NativeImage(
            spec,
            Projects.buildSubDirectory(getProject(), "uplift"),
            Projects.buildSubDirectory(getProject(), "uplift/classpath"),
            docker(),
            new GradleLog(getLogger())
        ).build(String.join(", ", Projects.dependencyTasks(this)));
    }

    /** Runtime classpath first, then the outputs of upstream tasks. Order is significant. */
    private List<Path> classPath() {
        List<Path> classPath = new ArrayList<>();
        if (getClassPath().isPresent()) {
            getClassPath().get().forEach(file -> classPath.add(file.toPath()));
        }
        Projects.dependencyOutputs(this).stream()
            .filter(FileIO::isJar)
            .forEach(classPath::add);
        return classPath;
    }

    private Docker docker() {
        return new Docker(
            Projects.resolveProperty(getProject(), Docker.BINARY_PROPERTY, null, Docker.DEFAULT_BINARY),
            new GradleLog(getLogger())
        );
    }
}
