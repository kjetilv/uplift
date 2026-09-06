package com.github.kjetilv.uplift.plugins.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/** Deploys the stack, then reports what is now running. */
@Mojo(name = "deploy", requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true)
public class DeployMojo extends AbstractLambdaZipMojo {

    @Override
    protected void perform() throws MojoExecutionException {
        collectLambdaZips();
        cdk().run("cdk deploy " + profileOption() + " --require-approval=never " + stack());
        report(stagedZips());
    }
}
