package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fio;
import com.github.kjetilv.uplift.fq.FqReader;
import com.github.kjetilv.uplift.util.Sleeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;

final class PathFqReader<I, O> extends AbstractPathFq<I, O>
    implements FqReader<O> {

    private static final Logger log = LoggerFactory.getLogger(PathFqReader.class);

    private final Collection<Path> processed = new HashSet<>();

    private Reader<I> currentReader;

    private Path currentPath;

    private final Function<Path, Reader<I>> readerFactory;

    private final boolean deleting;

    private final LongAdder count = new LongAdder();

    PathFqReader(
        Path path,
        Fio<I, O> fio,
        Function<Path, Reader<I>> readerFactory,
        Tombstone<Path> tombstone,
        boolean deleting
    ) {
        super(path, fio, tombstone);
        this.readerFactory = Objects.requireNonNull(readerFactory, "newPuller");
        this.deleting = deleting;
    }

    @Override
    public O next() {
        var nextLine = currentReader == null
            ? null
            : currentReader.read();

        if (nextLine != null) {
            return nextLine(nextLine);
        }

        var serial = new LongAdder();
        serial.increment();

        var sleeper = Sleeper.deferred(
            this::name,
            state ->
                log.warn("No files found after {}: {}", state.duration(), name())
        );

        while (true) {
            if (currentReader != null) {
                reset();
            }

            try (var pathStream = sortedFiles()) {
                var available = pathStream
                    .filter(this::candidate)
                    .toList();
                if (fileReady(available)) {
                    set(available.getFirst());
                } else {
                    if (foundTombstone()) {
                        return null;
                    }
                    sleeper.get().sleep();
                }
            }

            nextLine = currentReader == null
                ? null
                : currentReader.read();
            if (nextLine != null) {
                return nextLine(nextLine);
            }
        }
    }

    @Override
    protected void subToString(StringBuilder builder) {
        builder.append("processed: ").append(processed.size());
    }

    private boolean fileReady(List<Path> available) {
        return available.size() > 1 || available.size() == 1 && foundTombstone();
    }

    private O nextLine(I nextLine) {
        try {
            return fromInput(nextLine);
        } catch (Exception e) {
            throw new IllegalStateException(this + " failed to parse #" + count + ": " + nextLine, e);
        } finally {
            count.increment();
        }
    }

    private void set(Path first) {
        currentPath = first;
        currentReader = readerFactory.apply(currentPath);
    }

    private void reset() {
        currentReader.close();
        processed.add(currentPath);
        if (deleting) {
            rm(currentPath);
        }
        currentReader = null;
        currentPath = null;
    }

    private boolean candidate(Path path) {
        return !isTombstone(path) && !processed.contains(path);
    }
}
