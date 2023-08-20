package com.github.kjetilv.uplift.flogs;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

import com.github.kjetilv.flopp.qr.Qrs;
import com.github.kjetilv.flopp.qr.WriteQr;

final class FLoggers {

    private final LogLevel logLevel;

    private final Consumer<String> printer;

    private final ExecutorService executorService;

    private final LogEntryFormatter formatter;

    private final WriteQr<String> qr;

    FLoggers(LogLevel logLevel, Consumer<String> printer, ExecutorService executorService) {
        this.logLevel = logLevel == null ? LogLevel.INFO : logLevel;
        this.printer = printer == null ? System.out::println : printer;
        this.formatter = new LogEntryFormatter();

        this.executorService = executorService;
        if (this.executorService == null) {
            this.qr = null;
        } else {
            this.qr = Qrs.writer("logger", this.printer, 10);
            this.executorService.submit(this.qr);
        }
    }

    FLogger create(String name) {
        return new FLogger(name, logLevel, formatter, qr == null ? printer : qr, printer);
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
