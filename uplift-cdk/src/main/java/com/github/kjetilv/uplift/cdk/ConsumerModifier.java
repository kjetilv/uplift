package com.github.kjetilv.uplift.cdk;

import module aws.cdk.lib;
import module java.base;
import software.amazon.awscdk.Stack;

import java.util.function.Function;

@SuppressWarnings("unused")
record ConsumerModifier(List<Stacker> stackers) implements UpliftStacks.Session {

    @Override
    public void accept(Stack stack) {
        consumer().accept(stack);
    }

    @Override
    public ConsumerModifier modify(Function<Stacker, Stacker> modifier) {
        return new ConsumerModifier(
            stackers.stream()
                .map(modifier)
                .toList()
        );
    }

    @Override
    public Consumer<Stack> consumer() {
        return stackers.stream().reduce(
            new UpliftStacks(),
            UpliftStacks::add,
            (stacks, stacks2) -> {
                throw new IllegalStateException("Unexpected combone");
            }
        );
    }
}
