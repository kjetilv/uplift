package com.github.kjetilv.uplift.cdk;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import software.amazon.awscdk.Stack;

public final class UpliftStacks {

    private final List<Consumer<Stack>> builders = new ArrayList<>();

    public UpliftStacks add(Consumer<Stack> builder) {
        builders.add(builder);
        return this;
    }

    public void applyTo(Stack stack) {
        builders.forEach(builder -> builder.accept(stack));
    }
}
