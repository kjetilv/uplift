package com.github.kjetilv.uplift.json.samplegen;

import com.github.kjetilv.uplift.synchttp.HttpCallbackProcessor;
import com.github.kjetilv.uplift.synchttp.Server;
import com.github.kjetilv.uplift.synchttp.Servers;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.fail;

public class HttpTest {

    @Test
    void testHttpCallbacksStreamBody() throws Exception {
        var handler = new HttpCallbackProcessor((_, callbacks) ->
            callbacks.status(200).channel(channel -> {
                var writer = Users.INSTANCE.chunkedChannelWriter(50);
                writer.write(IOTest.USER, channel);
            }));

        switch (Servers.create().run(handler)) {
            case Servers.Result.OK(Server.Open run) -> {
                try (var httpClient = HttpClient.newHttpClient()) {
                    var response = httpClient.send(
                        HttpRequest.newBuilder(run.uri()).build(),
                        HttpResponse.BodyHandlers.ofString()
                    );
                    IO.println(response.body());
                }

                run.close();
                run.join();
            }
            case Servers.Result.Denied(InetSocketAddress address) -> fail("Could not bind to " + address);
        }
    }
}
