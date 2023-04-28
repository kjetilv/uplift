package uplift.examples.helloworld;

import java.util.function.Consumer;

import com.github.kjetilv.uplift.cdk.Stacker;
import com.github.kjetilv.uplift.cdk.UpliftStacks;
import software.amazon.awscdk.Stack;

@SuppressWarnings("unused")
public class HelloWorldBuilder implements Consumer<Stack> {

    @Override
    public void accept(Stack stack) {
        new UpliftStacks().add(
            Stacker.lambda("hello-world")
        ).applyTo(stack);
    }
}
