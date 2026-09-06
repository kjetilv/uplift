package com.github.kjetilv.uplift.plugins;

import com.github.kjetilv.uplift.plugins.core.FileIO;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

import java.nio.file.Path;
import java.util.List;

/** Stages the lambda zips where the CDK container can see them, under {@code /lambdas}. */
@CacheableTask
public abstract class UpliftLambdaZipTask extends UpliftTask {

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ListProperty<Path> getLambdaZips();

    protected final void collectLambdaZips() {
        List<Path> zips = lambdas();
        if (zips == null) {
            throw new IllegalStateException(
                "No zips configured, and no zips found in "
                + String.join(", ", Projects.dependencyTasks(this)));
        }
        zips.forEach(zip -> FileIO.copyTo(zip, upliftDir()));
    }

    /** Configured zips if any, else the zip outputs of upstream tasks, else null. */
    protected final List<Path> lambdas() {
        List<Path> configured = getLambdaZips().getOrElse(List.of());
        if (!configured.isEmpty()) {
            return List.copyOf(configured);
        }
        List<Path> found = Projects.dependencyOutputs(this).stream().filter(FileIO::isZip).toList();
        return found.isEmpty() ? null : found;
    }
}
