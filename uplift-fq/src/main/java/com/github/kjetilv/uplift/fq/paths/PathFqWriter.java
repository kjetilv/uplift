package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fio;
import com.github.kjetilv.uplift.fq.FqWriter;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;

final class PathFqWriter<I, T> extends AbstractPathFq<I, T> implements FqWriter<T> {

    private final Dimensions dimensions;

    private final Function<Path, Writer<I>> newWriter;

    private final LongAdder lineCount = new LongAdder();

    private final String suffix;

    private final String prefix;

    private Writer<I> currentWriter;

    private Ledge currentLedge;

    private Path currentPath;

    PathFqWriter(
        Path directory,
        Dimensions dimensions,
        Function<Path, Writer<I>> newWriter,
        Fio<I, T> fio,
        Tombstone<Path> tombstone
    ) {
        super(directory, fio, tombstone);
        this.dimensions = Objects.requireNonNull(dimensions, "dims");
        this.newWriter = Objects.requireNonNull(newWriter, "newWriter");

        var fileName = directory.getFileName().toString();
        var lastDot = fileName.lastIndexOf('.');

        this.suffix = fileName.substring(lastDot + 1);
        this.prefix = lastDot < 0 ? fileName : fileName.substring(0, lastDot);

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
        setTombstone(Instant.now().atZone(UTC).toString());
    }

    @Override
    protected void subToString(StringBuilder builder) {
        builder.append("current: ").append(currentPath);
    }

    private void writeItem(T item) {
        var count = lineCount.longValue();
        I line;
        try {
            line = toT(item);
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
        currentPath = path(ledge);
        currentWriter = newWriter.apply(currentPath);
        currentLedge = ledge;
    }

    private Path path(Ledge ledge) {
        var formatted = "%s-%s.%s".formatted(this.prefix, ledge.asSegment(), this.suffix);
        return directory().resolve(Path.of(formatted));
    }

    private static final ZoneId UTC = ZoneId.of("UTC");
}
