package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fio;
import com.github.kjetilv.uplift.fq.FqPuller;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;

final class PathFqPuller<I, T> extends AbstractPathFqReader<I, T> implements FqPuller<T> {

    private final Collection<Path> processed = new HashSet<>();

    private Puller<I> currentPuller;

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
        Backoff backoff = null;
        while (true) {
            var nextLine = currentPuller == null
                ? null
                : currentPuller.pull();
            if (nextLine != null) {
                try {
                    return Optional.ofNullable(fromBytes(nextLine));
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to parse #" + count, e);
                } finally {
                    count.increment();
                }
            }
            if (currentPuller != null) {
                currentPuller.close();
                processed.add(currentPuller.path());
                if (deleting) {
                    rm(currentPuller.path());
                }
            }
            currentPuller = sortedFiles().stream()
                .filter(path -> !ignored(processed, path))
                .findFirst()
                .map(newPuller)
                .orElse(null);
            if (currentPuller == null) {
                if (done()) {
                    return Optional.empty();
                }
                backoff = Backoff.sleepAndUpdate(name(), backoff);
            }
        }
    }

    private boolean ignored(Collection<Path> processed, Path path) {
        return isTombstone(path) || processed.contains(path);
    }
}
