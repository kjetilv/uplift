package uplift.examples.helloweb;

import java.util.function.Consumer;

import com.github.kjetilv.uplift.cdk.Stacker;
import com.github.kjetilv.uplift.cdk.UpliftStacks;
import software.amazon.awscdk.Stack;

@SuppressWarnings("unused")
public class HelloWebBuilder implements Consumer<Stack> {

    @Override
    public void accept(Stack stack) {
        new UpliftStacks().add(
            Stacker.lambda("hello-web").withUrl()
        ).applyTo(stack);
    }
}
