package com.github.kjetilv.uplift.lambda;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LambdaPayloadTest {

    @Test
    void canParse() {
        LambdaPayload payload = LambdaPayload.parse(SAMPLE);
        assertThat(payload.path("/audio/")).isEqualTo("QT59MOx-O_yKtqj1GXOQwpg.m4a");
        assertThat(payload.queryParam("t")).isEqualTo("ZDd-nVQrS9Ci4nujv1fuHw");
        assertThat(payload.pathParam("/audio/{path}.m4a", "path")).hasValue("QT59MOx-O_yKtqj1GXOQwpg");
        assertThat(payload.pathParam("*/{path}.m4a", "path")).hasValue("QT59MOx-O_yKtqj1GXOQwpg");
    }

    @Test
    void canParse2() {
        LambdaPayload payload = LambdaPayload.parse(SAMPLE);
        assertThat(payload.path("/audio/")).isEqualTo("QT59MOx-O_yKtqj1GXOQwpg.m4a");
        assertThat(payload.queryParam("t")).isEqualTo("ZDd-nVQrS9Ci4nujv1fuHw");
        assertThat(payload.pathParam("/audio/{path}.m4a", "path")).hasValue("QT59MOx-O_yKtqj1GXOQwpg");
        assertThat(payload.pathParam("*/{path}.m4a", "path")).hasValue("QT59MOx-O_yKtqj1GXOQwpg");
    }

    @Test
    void canParseVer1() {
        LambdaPayload payload = LambdaPayload.parse(VER_1);
        assertThat(payload.path()).isEqualTo("/my/path");
        assertThat(payload.method()).isEqualTo("GET");
        assertThat(payload.body()).isEqualTo("Hello from Lambda!");
        assertThat(payload.headers()).containsExactlyInAnyOrderEntriesOf(Map.of(
            "header1", "value1",
            "header2", "value2"
        ));
        assertThat(payload.queryParameters()).containsExactlyInAnyOrderEntriesOf(Map.of(
            "parameter1", "value1",
            "parameter2", "value"
        ));
    }

    @Test
    void canParseVer2() {
        LambdaPayload payload = LambdaPayload.parse(VER_2);
        assertThat(payload.path()).isEqualTo("/my/path");
        assertThat(payload.method()).isEqualTo("POST");
        assertThat(payload.body()).isEqualTo("Hello from Lambda");
        assertThat(payload.headers()).containsExactlyInAnyOrderEntriesOf(Map.of(
            "header1", "value1",
            "header2", "value1,value2"
        ));
        assertThat(payload.queryParameters()).containsExactlyInAnyOrderEntriesOf(Map.of(
            "parameter1", "value1,value2",
            "parameter2", "value"
        ));
//        assertThat(payload.queryParam("t")).isEqualTo("ZDd-nVQrS9Ci4nujv1fuHw");
//        assertThat(payload.pathParam("/audio/{path}.m4a", "path")).hasValue("QT59MOx-O_yKtqj1GXOQwpg");
//        assertThat(payload.pathParam("*/{path}.m4a", "path")).hasValue("QT59MOx-O_yKtqj1GXOQwpg");
    }

    @Test
    void canParseVer2a() {
        LambdaPayload payload = LambdaPayload.parse(VER_2_A);
        assertThat(payload.headers()).containsExactlyInAnyOrderEntriesOf(Map.of(
            "header1", "value1",
            "header2", "value2"
        ));
    }

    public static final String SAMPLE = """
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
            "path": "/audio/QT59MOx-O_yKtqj1GXOQwpg.m4a",
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
        """;

    private static final String VER_2 =
        //language=json
        """
            {
              "version": "2.0",
              "routeKey": "$default",
              "rawPath": "/my/path",
              "rawQueryString": "parameter1=value1&parameter1=value2&parameter2=value",
              "cookies": [
                "cookie1",
                "cookie2"
              ],
              "headers": {
                "header1": "value1",
                "header2": "value1,value2"
              },
              "queryStringParameters": {
                "parameter1": "value1,value2",
                "parameter2": "value"
              },
              "requestContext": {
                "accountId": "123456789012",
                "apiId": "api-id",
                "authentication": {
                  "clientCert": {
                    "clientCertPem": "CERT_CONTENT",
                    "subjectDN": "www.example.com",
                    "issuerDN": "Example issuer",
                    "serialNumber": "a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1",
                    "validity": {
                      "notBefore": "May 28 12:30:02 2019 GMT",
                      "notAfter": "Aug  5 09:36:04 2021 GMT"
                    }
                  }
                },
                "authorizer": {
                  "jwt": {
                    "claims": {
                      "claim1": "value1",
                      "claim2": "value2"
                    },
                    "scopes": [
                      "scope1",
                      "scope2"
                    ]
                  }
                },
                "domainName": "id.execute-api.us-east-1.amazonaws.com",
                "domainPrefix": "id",
                "http": {
                  "method": "POST",
                  "path": "/my/path",
                  "protocol": "HTTP/1.1",
                  "sourceIp": "192.0.2.1",
                  "userAgent": "agent"
                },
                "requestId": "id",
                "routeKey": "$default",
                "stage": "$default",
                "time": "12/Mar/2020:19:03:58 +0000",
                "timeEpoch": 1583348638390
              },
              "body": "Hello from Lambda",
              "pathParameters": {
                "parameter1": "value1"
              },
              "isBase64Encoded": false,
              "stageVariables": {
                "stageVariable1": "value1",
                "stageVariable2": "value2"
              }
            }
            """;

    private static final String VER_2_A =
        //language=json
        """
            {
              "headers": {
                "header1": "value1",
                "header2": "value2"
              }
            }
            """;

    private static final String VER_1 =
        //language=json
        """
                {
                  "version": "1.0",
                  "resource": "/my/path",
                  "path": "/my/path",
                  "httpMethod": "GET",
                  "headers": {
                    "header1": "value1",
                    "header2": "value2"
                  },
                  "multiValueHeaders": {
                    "header1": [
                      "value1"
                    ],
                    "header2": [
                      "value1",
                      "value2"
                    ]
                  },
                  "queryStringParameters": {
                    "parameter1": "value1",
                    "parameter2": "value"
                  },
                  "multiValueQueryStringParameters": {
                    "parameter1": [
                      "value1",
                      "value2"
                    ],
                    "parameter2": [
                      "value"
                    ]
                  },
                  "requestContext": {
                    "accountId": "123456789012",
                    "apiId": "id",
                    "authorizer": {
                      "claims": null,
                      "scopes": null
                    },
                    "domainName": "id.execute-api.us-east-1.amazonaws.com",
                    "domainPrefix": "id",
                    "extendedRequestId": "request-id",
                    "httpMethod": "GET",
                    "identity": {
                      "accessKey": null,
                      "accountId": null,
                      "caller": null,
                      "cognitoAuthenticationProvider": null,
                      "cognitoAuthenticationType": null,
                      "cognitoIdentityId": null,
                      "cognitoIdentityPoolId": null,
                      "principalOrgId": null,
                      "sourceIp": "192.0.2.1",
                      "user": null,
                      "userAgent": "user-agent",
                      "userArn": null,
                      "clientCert": {
                        "clientCertPem": "CERT_CONTENT",
                        "subjectDN": "www.example.com",
                        "issuerDN": "Example issuer",
                        "serialNumber": "a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1",
                        "validity": {
                          "notBefore": "May 28 12:30:02 2019 GMT",
                          "notAfter": "Aug  5 09:36:04 2021 GMT"
                        }
                      }
                    },
                    "path": "/my/path",
                    "protocol": "HTTP/1.1",
                    "requestId": "id=",
                    "requestTime": "04/Mar/2020:19:15:17 +0000",
                    "requestTimeEpoch": 1583349317135,
                    "resourceId": null,
                    "resourcePath": "/my/path",
                    "stage": "$default"
                  },
                  "pathParameters": null,
                  "stageVariables": null,
                  "body": "Hello from Lambda!",
                  "isBase64Encoded": false
                }
            """;
}
