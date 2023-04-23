package uplift.flambda;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import uplift.asynchttp.HttpResponse;
import uplift.kernel.io.BytesIO;

record LambdaResponse(
    int statusCode,
    Map<String, List<String>> headers,
    String body,
    boolean isBase64Encoded
) {

    HttpResponse toHttpResponse() {
        return new HttpResponse(statusCode(), headers(), resolveBody());
    }

    private byte[] resolveBody() {
        return body == null || body.length() == 0 ? BytesIO.NOBODY
            : isBase64Encoded() ? BytesIO.fromBase64(body())
                : body().getBytes(StandardCharsets.UTF_8);
    }
}
