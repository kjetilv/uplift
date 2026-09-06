package com.github.kjetilv.uplift.plugins.maven;

import com.github.kjetilv.uplift.plugins.core.FileIO;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/** Destroys the stack, then removes the generated CDK application. */
@Mojo(name = "destroy", requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class DestroyMojo extends AbstractCdkMojo {

    @Override
    protected void perform() {
        if (!FileIO.isActualDirectory(cdkApp())) {
            initCdkApp();
        }
        cdk().run("cdk destroy --require-approval=never " + profileOption() + " " + stack());
        FileIO.clearRecursive(cdkApp());
    }
}
