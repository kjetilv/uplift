package uplift.lambda;

@FunctionalInterface
public interface LambdaHandler {

    LambdaResult handle(LambdaPayload lambdaPayload);
}
