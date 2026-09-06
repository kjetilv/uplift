package com.github.kjetilv.uplift.plugins.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.IOException;
import java.nio.file.Files;

/** Deploys the stack, then reports what is now running. */
@Mojo(name = "deploy", requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true)
public class DeployMojo extends AbstractLambdaZipMojo {

    @Override
    protected void perform() throws MojoExecutionException {
        ensureCdkApp();
        collectLambdaZips();
        // Before the deploy, not after: a check that runs afterwards can only report damage.
        var sources = stagedZips();
        sources.forEach(source -> {
            if (!Files.isRegularFile(source)) {
                throw new IllegalStateException("No such file: " + source);
            }
            long size;
            try {
                size = Files.size(source);
            } catch (IOException e) {
                throw new IllegalStateException("Could not size " + source, e);
            }
            if (size <= 0) {
                throw new IllegalStateException("Empty or corrupt file, size " + size + ": " + source);
            }
        });
        cdk().run("cdk deploy " + profileOption() + " --require-approval=never " + stack());
        report(sources);
    }
}
