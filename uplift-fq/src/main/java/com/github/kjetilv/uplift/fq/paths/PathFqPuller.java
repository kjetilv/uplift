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
        Sleeper sleeper = null;
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
                (sleeper = sleeper(sleeper, this::name)).sleep();
            }
        }
    }

    private boolean ignored(Collection<Path> processed, Path path) {
        return isTombstone(path) || processed.contains(path);
    }

    private static Sleeper sleeper(Sleeper sleeper, Supplier<String> description) {
        return Objects.requireNonNullElseGet(
            sleeper, () -> new Sleeper(
                description.get(),
                state ->
                    log.warn("No files found after {}: {}", state.duration(), description.get())
            )
        );
    }
}
