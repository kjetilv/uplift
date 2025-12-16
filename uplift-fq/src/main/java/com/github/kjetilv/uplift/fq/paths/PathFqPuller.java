package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fio;
import com.github.kjetilv.uplift.fq.FqPuller;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Predicate;

final class PathFqPuller<T> extends AbstractPathFqReader<T> implements FqPuller<T> {

    private final Collection<Path> processed = new HashSet<>();

    private Puller currentPuller;

    private final boolean deleting;

    private final LongAdder count = new LongAdder();

    PathFqPuller(Path path, Fio<T> fio, boolean deleting, Charset cs) {
        super(path, fio, cs);
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
                .filter(candidate(processed))
                .findFirst()
                .map(path ->
                    new Puller(path, inputStream(path)))
                .orElse(null);
            if (currentPuller == null) {
                if (done()) {
                    return Optional.empty();
                }
                backoff = Backoff.sleepAndUpdate(name(), backoff);
            }
        }
    }

    private Predicate<Path> candidate(Collection<Path> processed) {
        return path -> !path.equals(tombstone()) && !processed.contains(path);
    }
}
