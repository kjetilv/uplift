package com.github.kjetilv.uplift.json;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;

public class ToughOneTest {

    @Test
    void sirParseALot() {
        var map = Json.instance().jsonMap(
            //language=json
            """
            {
              "isBase64Encoded": true,
              "headers": {
               "content-range": "bytes 208-209/256",
               "accept-ranges": "bytes",
               "content-length": "2",
               "connection": "keep-alive",
               "content-type": "audio/m4a",
               "zit": []
              },
              "body": "ubk=",
              "statusCode": 206,
              "zot": [{
                "bop": []
              }]
            }
            """
        );
        assertThat(map.get("isBase64Encoded")).isEqualTo(true);

        assertThat(map.get("zot")).asInstanceOf(LIST).singleElement().satisfies(obj ->
            assertThat(obj).asInstanceOf(MAP).containsExactlyEntriesOf(
                Map.of("bop", List.of())));
    }
}
