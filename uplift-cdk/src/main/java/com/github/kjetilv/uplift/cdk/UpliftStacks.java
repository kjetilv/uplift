package com.github.kjetilv.uplift.cdk;

import software.amazon.awscdk.Stack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public final class UpliftStacks implements Consumer<Stack> {

    public static Session modules(String... modules) {
        return new ConsumerModifier(
            Arrays.stream(modules)
                .map(Stacker::lambda)
                .toList());
    }

    private final List<Consumer<Stack>> builders = new ArrayList<>();

    @Override
    public void accept(Stack stack) {
        applyTo(stack);
    }

    public UpliftStacks add(Consumer<Stack> builder) {
        builders.add(builder);
        return this;
    }

    public void applyTo(Stack stack) {
        builders.forEach(builder ->
            builder.accept(stack));
    }

    public interface Session extends Consumer<Stack> {

        Consumer<Stack> modify(Function<Stacker, Stacker> modifier);

        Consumer<Stack> consumer();
    }
}
