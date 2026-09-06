package com.github.kjetilv.uplift.plugins;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.OutputFile;

import java.nio.file.Path;

/** Bootstraps the CDK toolkit stack in the target account and region. */
@CacheableTask
public abstract class UpliftBootstrapTask extends UpliftLambdaZipTask {

    @OutputFile
    public abstract Property<Path> getTemplate();

    @Override
    protected void perform() {
        collectLambdaZips();
        cdk().run("cdk bootstrap " + profileOption() + " aws://" + getAccount().get() + "/" + getRegion().get());
    }
}
