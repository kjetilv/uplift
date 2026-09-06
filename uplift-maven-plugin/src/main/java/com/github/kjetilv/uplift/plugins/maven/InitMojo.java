package com.github.kjetilv.uplift.plugins.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/** Generates the CDK application. Touches no AWS resources. */
@Mojo(name = "init", requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class InitMojo extends AbstractCdkMojo {

    @Override
    protected void perform() throws MojoExecutionException {
        initCdkApp();
    }
}
