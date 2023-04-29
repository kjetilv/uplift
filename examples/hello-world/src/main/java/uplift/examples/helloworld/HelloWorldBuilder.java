package uplift.examples.helloworld;

import java.util.function.Consumer;

import com.github.kjetilv.uplift.cdk.UpliftStacks;
import software.amazon.awscdk.Stack;

@SuppressWarnings("unused")
public class HelloWorldBuilder implements Consumer<Stack> {

    @Override
    public void accept(Stack stack) {
        UpliftStacks.modules("hello-world").accept(stack);
    }
}
