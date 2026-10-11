package com.github.kjetilv.uplift.synchttp;

import module java.base;
import module java.net.http;
import com.github.kjetilv.uplift.util.Print;
import com.github.kjetilv.uplift.util.RuntimeCloseable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;

public interface Server {

    interface Init extends Server {

        default Server.Open open(Processor processor) {
            return switch (run(processor)) {
                case Servers.Result.OK<Open>(Server.Open result) -> result;
                case Servers.Result.Denied<Server.Open>(InetSocketAddress address) -> throw new IllegalStateException(
                    "Failed to open " + processor + " on " + address);
            };
        }

        Servers.Result<Server.Open> run(Processor processor);
    }

    interface Open extends Server, RuntimeCloseable {

        Logger log = LoggerFactory.getLogger(Server.Open.class);

        @SuppressWarnings("HttpUrlsUsage")
        default URI uri() {
            var address = address();
            return URI.create("http://%s:%d".formatted(address.getHostName(), address.getPort()));
        }

        default Servers.Result<Server.Closed> await(String name) {
            var uptime = Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime());
            log.info("{} startup @ {}", name, Print.shortTime(uptime));
            join();
            return new Servers.Result.OK<>(() -> uptime);
        }

        InetSocketAddress address();

        void join();
    }

    interface Closed extends Server {

        Duration time();
    }

    interface Processor extends RuntimeCloseable {

        boolean process(
            ReadableByteChannel in,
            WritableByteChannel out
        );
    }
}
