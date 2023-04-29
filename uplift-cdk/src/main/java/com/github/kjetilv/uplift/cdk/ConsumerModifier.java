package com.github.kjetilv.uplift.cdk;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import software.amazon.awscdk.Stack;

@SuppressWarnings("unused")
record ConsumerModifier(List<Stacker> stackers) implements UpliftStacks.Session {

    @Override
    public void accept(Stack stack) {
        consumer().accept(stack);
    }

    @Override
    public ConsumerModifier modify(Function<Stacker, Stacker> modifier) {
        return new ConsumerModifier(
            stackers.stream().map(modifier).toList()
        );
    }

    @Override
    public Consumer<Stack> consumer() {
        return stackers.stream().reduce(
            new UpliftStacks(),
            UpliftStacks::add,
            (stacks, stacks2) -> {
                throw new IllegalStateException("Unexpected combone");
            });
    }
}
