package com.github.kjetilv.uplift.flogs;

import com.github.kjetilv.flopp.kernel.qr.Qrs;
import com.github.kjetilv.flopp.kernel.qr.WriteQr;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class FLoggers {

    private final LogLevel logLevel;

    private final Consumer<String> printer;

    private final ExecutorService executorService;

    private final Supplier<Instant> time;

    private final LogFormatter<LogEntry> formatter;

    private final WriteQr<String> qr;

    FLoggers(
        LogLevel logLevel,
        Consumer<String> printer,
        Supplier<Instant> time,
        ExecutorService executorService,
        LogFormatter<LogEntry> formatter
    ) {
        this.logLevel = logLevel == null ? LogLevel.INFO : logLevel;
        this.printer = printer == null ? System.out::println : printer;
        this.time = Objects.requireNonNull(time, "time");
        this.formatter = formatter == null ? LogEntryFormatter.INSTANCE : formatter;

        this.executorService = executorService;
        if (this.executorService != null) {
            this.qr = Qrs.writer("logger", this.printer, 10);
            this.executorService.submit(this.qr);
        } else {
            this.qr = null;
        }
    }

    FLogger create(String name) {
        return new FLogger(
            name,
            logLevel,
            formatter,
            qr == null ? printer : qr,
            printer,
            time
        );
    }

    void close() {
        if (this.qr != null) {
            try {
                qr.close();
            } finally {
                executorService.shutdown();
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + logLevel + "]";
    }
}
