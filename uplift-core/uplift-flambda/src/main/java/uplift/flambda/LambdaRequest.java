package uplift.flambda;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import uplift.asynchttp.HttpRequest;
import uplift.kernel.io.BytesIO;

import static java.util.Objects.requireNonNull;

public record LambdaRequest(
    String id,
    HttpRequest request
) {

    public LambdaRequest(HttpRequest httpRequest) {
        this(null, httpRequest);
    }

    public LambdaRequest(String id, HttpRequest request) {
        this.id = id;
        this.request = requireNonNull(request, "request");
    }

    public LambdaRequest withId(String id) {
        return new LambdaRequest(requireNonNull(id, "id"), request);
    }

    JsonLambdaPayload toJsonPayload() {
        return new JsonLambdaPayload(
            "2.0",
            request.method(),
            request.path(),
            toSingleValue(request.headers()),
            toSingleValue(request.queryParams()),
            new JsonLambdaPayload.RequestContext(
                new JsonLambdaPayload.RequestContext.Http(
                    request.method(),
                    request.path()
                )),
            true,
            BytesIO.toBase64(request.body())
        );
    }

    private static Map<String, String> toSingleValue(Map<String, ? extends List<String>> map) {
        return map.entrySet().stream().collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry -> String.join(",", entry.getValue())
            )
        );
    }

    record JsonLambdaPayload(
        String version,
        String httpMethod,
        String path,
        Map<String, String> headers,
        Map<String, String> queryStringParameters,
        RequestContext requestContext,
        boolean isBase64Encoded,
        String body
    ) {

        @SuppressWarnings("WeakerAccess")
        record RequestContext(
            Http http
        ) {

            record Http(
                String method,
                String path
            ) {

            }
        }
    }
}
