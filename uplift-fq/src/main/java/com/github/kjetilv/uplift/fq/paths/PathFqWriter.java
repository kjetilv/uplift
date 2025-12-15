package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fio;
import com.github.kjetilv.uplift.fq.FqWriter;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;

final class PathFqWriter<T> extends AbstractPathFq<T> implements FqWriter<T> {

    private final Dimensions dimensions;

    private final LongAdder lineCount = new LongAdder();

    private final String suffix;

    private final String prefix;

    private Writer currentWriter;

    private Ledge currentLedge;

    PathFqWriter(Path directory, Dimensions dimensions, Fio<T> fio, Charset cs) {
        super(directory, fio, cs);
        this.dimensions = Objects.requireNonNull(dimensions, "dims");

        var fileName = directory.getFileName().toString();
        var lastDot = fileName.lastIndexOf('.');

        this.suffix = fileName.substring(lastDot + 1);
        this.prefix = fileName.substring(0, lastDot);

        failOnTombstone();
    }

    @Override
    public void write(List<T> items) {
        items.forEach(this::writeItem);
    }

    @Override
    public boolean done() {
        return foundTombstone();
    }

    @Override
    public void close() {
        if (currentWriter != null) {
            try {
                currentWriter.close();
            } catch (Exception e) {
                throw new RuntimeException("Failed to close", e);
            }
        }
        try (var tombstoneWriter = tombstoneWriter()) {
            new Writer(tombstoneWriter).write(Instant.now().atZone(UTC).toString()).close();
        } catch (Exception e) {
            throw new RuntimeException("Could not set tombstone", e);
        }
    }

    private void writeItem(T item) {
        var count = lineCount.longValue();
        String line;
        try {
            line = toString(item);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write #" + count + ": " + item, e);
        }
        var ledge = dimensions.ledge(count);
        if (!ledge.equals(currentLedge)) {
            switchWriter(ledge);
        }
        try {
            currentWriter.write(line);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write #" + count + ": " + line, e);
        } finally {
            lineCount.increment();
        }
    }

    private void switchWriter(Ledge ledge) {
        if (currentWriter != null) {
            currentWriter.close();
        }
        currentWriter = new Writer(bufferedWriter(path(ledge)));
        currentLedge = ledge;
    }

    private Path path(Ledge ledge) {
        var formatted = "%s-%s.%s.gz".formatted(this.prefix, ledge.asSegment(), this.suffix);
        return directory().resolve(Path.of(formatted));
    }

    private static final ZoneId UTC = ZoneId.of("UTC");
}
