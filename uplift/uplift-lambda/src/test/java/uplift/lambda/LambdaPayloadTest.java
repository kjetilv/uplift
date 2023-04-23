package uplift.lambda;

import java.util.Map;

import uplift.json.Json;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LambdaPayloadTest {

    @Test
    void canParse() {
        Map<String, Object> map = Json.INSTANCE.jsonMap(
            """
            {
              "resource": "/audio/{track}",
              "path": "/audio/QT59MOx-O_yKtqj1GXOQwpg.m4a",
              "httpMethod": "GET",
              "headers": {
                "Accept": "*/*",
                "Accept-Encoding": "br,deflate,gzip,x-gzip",
                "Host": "ghrqcprfuf.execute-api.eu-north-1.amazonaws.com",
                "Range": "bytes=0-",
                "User-Agent": "Apache-HttpClient/4.5.13 (Java/17.0.5)",
                "X-Amzn-Trace-Id": "Root=1-63bd45b6-4d38a2b66304e79e57da66eb",
                "X-Forwarded-For": "45.84.39.107",
                "X-Forwarded-Port": "443",
                "X-Forwarded-Proto": "https"
              },
              "multiValueHeaders": {
                "Accept": [
                  "*/*"
                ],
                "Accept-Encoding": [
                  "br,deflate,gzip,x-gzip"
                ],
                "Host": [
                  "ghrqcprfuf.execute-api.eu-north-1.amazonaws.com"
                ],
                "Range": [
                  "bytes=0-"
                ],
                "User-Agent": [
                  "Apache-HttpClient/4.5.13 (Java/17.0.5)"
                ],
                "X-Amzn-Trace-Id": [
                  "Root=1-63bd45b6-4d38a2b66304e79e57da66eb"
                ],
                "X-Forwarded-For": [
                  "45.84.39.107"
                ],
                "X-Forwarded-Port": [
                  "443"
                ],
                "X-Forwarded-Proto": [
                  "https"
                ]
              },
              "queryStringParameters": {
                "t": "ZDd-nVQrS9Ci4nujv1fuHw"
              },
              "multiValueQueryStringParameters": {
                "t": [
                  "ZDd-nVQrS9Ci4nujv1fuHw"
                ]
              },
              "pathParameters": {
                "track": "QT59MOx-O_yKtqj1GXOQwpg.m4a"
              },
              "requestContext": {
                "resourceId": "4r6lx4",
                "resourcePath": "/audio/{track}",
                "httpMethod": "GET",
                "extendedRequestId": "ehfUhFJaAi0Fa-A=",
                "requestTime": "10/Jan/2023:11:02:14 +0000",
                "path": "/prod/audio/QT59MOx-O_yKtqj1GXOQwpg.m4a",
                "accountId": "732946774009",
                "protocol": "HTTP/1.1",
                "stage": "prod",
                "domainPrefix": "ghrqcprfuf",
                "requestTimeEpoch": 1673348534302,
                "requestId": "d68ec2f0-4bf6-43ad-934a-3946949fbb3e",
                "identity": {
                  "sourceIp": "45.84.39.107",
                  "userAgent": "Apache-HttpClient/4.5.13 (Java/17.0.5)"
                },
                "domainName": "ghrqcprfuf.execute-api.eu-north-1.amazonaws.com",
                "apiId": "ghrqcprfuf"
              },
              "isBase64Encoded": false
            }
            """);
        LambdaPayload payload = LambdaPayload.create(map);
        assertThat(payload.path("/audio/")).isEqualTo("QT59MOx-O_yKtqj1GXOQwpg.m4a");
        assertThat(payload.queryParam("t")).isEqualTo("ZDd-nVQrS9Ci4nujv1fuHw");
        assertThat(payload.pathParam("/audio/{path}.m4a", "path")).hasValue("QT59MOx-O_yKtqj1GXOQwpg");
        assertThat(payload.pathParam("*/{path}.m4a", "path")).hasValue("QT59MOx-O_yKtqj1GXOQwpg");
    }
}
