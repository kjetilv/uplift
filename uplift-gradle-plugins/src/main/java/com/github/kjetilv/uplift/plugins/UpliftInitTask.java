package com.github.kjetilv.uplift.plugins;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.OutputFile;

import java.nio.file.Path;

/** Generates the CDK application. Touches no AWS resources. */
@CacheableTask
public abstract class UpliftInitTask extends UpliftCdkTask {

    @OutputFile
    public abstract Property<Path> getActiveJar();

    @OutputFile
    public abstract Property<Path> getActivePom();

    @Override
    protected void perform() {
        initCdkApp();
    }
}
