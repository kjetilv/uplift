package com.github.kjetilv.uplift.json;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ToughOneTest {

    @Test
    void sirParseALot() {
        Map<String, Object> map = Json.INSTANCE.jsonMap(
            """
            {
              "isBase64Encoded": true,
              "headers": {
               "content-range": "bytes 208-209/256",
               "accept-ranges": "bytes",
               "content-length": "2",
               "connection": "keep-alive",
               "content-type": "audio/m4a"
              },
              "body": "ubk=",
              "statusCode": 206
            }
            """
        );
        assertThat(map.get("isBase64Encoded")).isEqualTo(true);
    }

}
