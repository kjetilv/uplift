package com.github.kjetilv.uplift.json.samplegen;

import com.github.kjetilv.uplift.synchttp.HttpCallbackProcessor;
import com.github.kjetilv.uplift.synchttp.Server;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpTest {

    @Test
    void testHttpCallbacksStreamBody() throws Exception {
        var handler = new HttpCallbackProcessor((_, callbacks) ->
            callbacks.status(200).channel(channel -> {
                var writer = Users.INSTANCE.chunkedChannelWriter(50);
                writer.write(IOTest.USER, channel);
            }));

        var run = Server.create().run(handler);

        try (var httpClient = HttpClient.newHttpClient()) {
            var response = httpClient.send(
                HttpRequest.newBuilder(run.uri()).build(),
                HttpResponse.BodyHandlers.ofString()
            );
            System.out.println(response.body());
        }

        run.close();
        run.join();
    }

}
