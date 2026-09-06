package com.github.kjetilv.uplift.plugins;

import com.github.kjetilv.uplift.plugins.core.FileIO;
import org.gradle.work.DisableCachingByDefault;

/** Destroys the stack, then removes the generated CDK application. */
@DisableCachingByDefault(because = "Destroying a stack is not cacheable")
public abstract class UpliftDestroyTask extends UpliftCdkTask {

    @Override
    protected void perform() {
        if (!FileIO.isActualDirectory(cdkApp())) {
            initCdkApp();
        }
        cdk().run("cdk destroy --require-approval=never " + profileOption() + " " + getStack().get());
        FileIO.clearRecursive(cdkApp());
    }
}
