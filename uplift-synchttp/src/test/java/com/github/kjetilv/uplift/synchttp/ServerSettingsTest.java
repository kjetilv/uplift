package com.github.kjetilv.uplift.synchttp;

import module java.base;
import org.junit.jupiter.api.Test;

import static com.github.kjetilv.uplift.synchttp.TestConnections.get;
import static com.github.kjetilv.uplift.synchttp.TestConnections.halfClosedExchange;
import static org.assertj.core.api.Assertions.assertThat;

class ServerSettingsTest {

    @Test
    void defaults() {
        assertThat(ServerSettings.DEFAULT.readTimeout()).isEqualTo(Duration.ofSeconds(60));
        assertThat(ServerSettings.DEFAULT.backlog()).isEqualTo(1024);
    }

    @Test
    void invalidValuesGiveDefaults() {
        assertThat(new ServerSettings(Duration.ZERO, 0)).isEqualTo(ServerSettings.DEFAULT);
        assertThat(new ServerSettings(Duration.ofSeconds(-1), -5)).isEqualTo(ServerSettings.DEFAULT);
    }

    @Test
    void withers() {
        var settings = ServerSettings.DEFAULT.withBacklog(4096).withReadTimeout(Duration.ofSeconds(5));
        assertThat(settings.backlog()).isEqualTo(4096);
        assertThat(settings.readTimeout()).isEqualTo(Duration.ofSeconds(5));
    }

    @Test
    void serverWithBacklog() {
        var address = new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);
        var settings = ServerSettings.DEFAULT.withBacklog(10);
        HttpHandler handler = (_, callback) -> callback.status(200).contentLength(2).body("ok");
        try (var server = Servers.create(address, settings).open(new HttpCallbackProcessor(handler))) {
            assertThat(halfClosedExchange(server, get("/"))).endsWith("\r\n\r\nok");
        }
    }
}
