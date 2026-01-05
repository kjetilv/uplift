package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fio;
import com.github.kjetilv.uplift.fq.FqReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

final class PathFqReader<I, O> extends AbstractPathFq<I, O>
    implements FqReader<O> {

    private static final Logger log = LoggerFactory.getLogger(PathFqReader.class);

    private final Collection<Path> processed = new HashSet<>();

    private Reader<I> currentReader;

    private Path currentPath;

    private final Function<Path, Reader<I>> readerFactory;

    private final Runnable awaitNext;

    private final boolean deleting;

    private final LongAdder count = new LongAdder();

    PathFqReader(
        Path path,
        Fio<I, O> fio,
        Function<Path, Reader<I>> readerFactory,
        Runnable awaitNext,
        Tombstone<Path> tombstone,
        boolean deleting
    ) {
        super(path, fio, tombstone);
        this.readerFactory = requireNonNull(readerFactory, "readerFactory");
        this.awaitNext = awaitNext;
        this.deleting = deleting;
    }

    @Override
    public O next() {
        var nextLine = currentReader == null ? null : readNext();

        if (nextLine != null) {
            return nextLine(nextLine);
        }

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
                    awaitNext.run();
                }
            }

            nextLine = currentReader == null ? null : readNext();
            if (nextLine != null) {
                return nextLine(nextLine);
            }
        }
    }

    @Override
    protected void subToString(StringBuilder builder) {
        builder.append("processed: ").append(processed.size());
    }

    private I readNext() {
        try {
            return currentReader.read();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read #" + count + " from " + currentPath, e);
        }
    }

    private boolean fileReady(List<Path> available) {
        return available.size() > 1 || available.size() == 1 && foundTombstone();
    }

    private O nextLine(I nextLine) {
        O o;
        try {
            o = fromInput(nextLine);
        } catch (Exception e) {
            throw new IllegalStateException(this + " failed to parse #" + count + ": " + nextLine, e);
        }
        count.increment();
        return o;
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
