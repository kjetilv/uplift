package com.github.kjetilv.uplift.plugins;

import org.gradle.work.DisableCachingByDefault;

import java.nio.file.Path;
import java.util.List;

/** Deploys the stack, then reports what is now running. */
@DisableCachingByDefault(because = "Deploying a stack is not cacheable")
public abstract class UpliftDeployTask extends UpliftLambdaZipTask {

    @Override
    protected void perform() {
        collectLambdaZips();
        cdk().run("cdk deploy " + profileOption() + " --require-approval=never " + getStack().get());
        List<Path> lambdas = lambdas();
        report(lambdas == null ? List.of() : lambdas);
    }
}
