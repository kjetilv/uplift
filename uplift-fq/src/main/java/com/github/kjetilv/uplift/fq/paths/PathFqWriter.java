package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fio;
import com.github.kjetilv.uplift.fq.FqWriter;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

final class PathFqWriter<I, O> extends AbstractPathFq<I, O>
    implements FqWriter<O> {

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
        Fio<I, O> fio,
        Tombstone<Path> tombstone
    ) {
        super(directory, fio, tombstone);
        this.dimensions = requireNonNull(dimensions, "dims");
        this.newWriter = requireNonNull(newWriter, "newWriter");

        var fileName = directory.getFileName().toString();
        var lastDot = fileName.lastIndexOf('.');

        this.suffix = fileName.substring(lastDot + 1);
        this.prefix = lastDot < 0 ? fileName : fileName.substring(0, lastDot);

        failOnTombstone();
    }

    @Override
    public void write(List<O> items) {
        for (O item : items) {
            var count = lineCount.longValue();
            var ledge = dimensions.ledge(count);
            if (currentLedge == null || currentLedge.ledge() != ledge.ledge()) {
                updateWriter(ledge);
            }
            var value = parse(item, count);
            write(value, count);
        }
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

    private I parse(O item, long count) {
        try {
            return toOutput(item);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write #" + count + ": " + item, e);
        }
    }

    private void updateWriter(Ledge ledge) {
        if (currentWriter != null) {
            currentWriter.close();
        }
        currentPath = path(ledge.asSegment());
        currentLedge = ledge;
        currentWriter = newWriter.apply(currentPath);
    }

    private void write(I line, long count) {
        try {
            currentWriter.write(line);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write #" + count + ": " + line, e);
        } finally {
            lineCount.increment();
        }
    }

    private Path path(String segment) {
        var formatted = "%s-%s.%s".formatted(this.prefix, segment, this.suffix);
        return directory().resolve(Path.of(formatted));
    }

    private static final ZoneId UTC = ZoneId.of("UTC");
}
