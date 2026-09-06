package com.github.kjetilv.uplift.plugins.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Synthesises the CloudFormation template without contacting AWS.
 * <p>
 * The Gradle plugin had no equivalent, which made the CDK path impossible to check without
 * a real deploy. It is the verification step for everything the deploy goal would do.
 */
@Mojo(name = "synth", requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true)
public class SynthMojo extends AbstractLambdaZipMojo {

    @Override
    protected void perform() throws MojoExecutionException {
        ensureCdkApp();
        collectLambdaZips();
        cdk().run("cdk synth " + profileOption() + " " + stack());
    }
}
