package com.kjetilv.github.uplift.plugins.jenesis;

import module java.base;
import build.jenesis.*;

public class UpliftFactory implements BuildModuleFactory {

    @Override
    public BuildExecutorModule create(
        Environment environment,
        SequencedMap<String, String> properties,
        Permits permits,
        String group,
        Map<String, Repository> repositories,
        Map<String, Resolver> resolvers
    ) {
        return (buildExecutor, inherited) -> {
            buildExecutor.addStep(
                "uplift-lambda",
                (
                    executor,
                    context,
                    arguments
                ) ->
                    CompletableFuture.completedFuture(new BuildStepResult(false))
            );
        };
    }
}
