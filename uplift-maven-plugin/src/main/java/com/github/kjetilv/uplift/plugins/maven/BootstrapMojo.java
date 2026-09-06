package com.github.kjetilv.uplift.plugins.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/** Bootstraps the CDK toolkit stack in the target account and region. */
@Mojo(name = "bootstrap", requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true)
public class BootstrapMojo extends AbstractLambdaZipMojo {

    @Override
    protected void perform() throws MojoExecutionException {
        collectLambdaZips();
        cdk().run("cdk bootstrap " + profileOption() + " aws://" + account + "/" + region);
    }
}
