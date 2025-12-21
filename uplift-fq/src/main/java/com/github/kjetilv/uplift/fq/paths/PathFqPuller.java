package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fio;
import com.github.kjetilv.uplift.fq.FqPuller;
import com.github.kjetilv.uplift.fq.Tombstone;
import com.github.kjetilv.uplift.util.Sleeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.function.Supplier;

final class PathFqPuller<I, T> extends AbstractPathFqReader<I, T> implements FqPuller<T> {

    private static final Logger log = LoggerFactory.getLogger(PathFqPuller.class);

    private final Collection<Path> processed = new HashSet<>();

    private Puller<I> currentPuller;

    private Path currentPath;

    private final Function<Path, Puller<I>> newPuller;

    private final boolean deleting;

    private final LongAdder count = new LongAdder();

    PathFqPuller(
        Path path,
        Fio<I, T> fio,
        Function<Path, Puller<I>> newPuller,
        Tombstone<Path> tombstone,
        boolean deleting
    ) {
        super(path, fio, tombstone);
        this.newPuller = Objects.requireNonNull(newPuller, "newPuller");
        this.deleting = deleting;
    }

    @Override
    public Optional<T> next() {
        var nextLine = currentPuller == null
            ? null
            : currentPuller.pull();
        if (nextLine != null) {
            return nextLine(nextLine);
        }
        Supplier<Sleeper> sleeper = Sleeper.create(
            this::name,
            state ->
                log.warn("No files found after {}: {}", state.duration(), name())
        );

        while (true) {
            if (currentPuller != null) {
                reset();
            }

            try (var pathStream = sortedFiles()) {
                var available = pathStream.filter(this::candidate)
                    .toList();
                if (available.size() > 1 || available.size() == 1 && done()) {
                    set(available.getFirst());
                } else {
                    if (done()) {
                        return Optional.empty();
                    }
                    sleeper.get().sleep();
                }
            }

            nextLine = currentPuller == null
                ? null
                : currentPuller.pull();
            if (nextLine != null) {
                return nextLine(nextLine);
            }
        }
    }

    @Override
    protected void subToString(StringBuilder builder) {
        builder.append("processed: ").append(processed.size());
    }

    private Optional<T> nextLine(I nextLine) {
        try {
            return Optional.ofNullable(fromBytes(nextLine));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse #" + count, e);
        } finally {
            count.increment();
        }
    }

    private void set(Path first) {
        currentPath = first;
        currentPuller = newPuller.apply(currentPath);
    }

    private void reset() {
        currentPuller.close();
        processed.add(currentPath);
        if (deleting) {
            rm(currentPath);
        }
        currentPuller = null;
        currentPath = null;
    }

    private boolean candidate(Path path) {
        return !isTombstone(path) && !processed.contains(path);
    }

}
