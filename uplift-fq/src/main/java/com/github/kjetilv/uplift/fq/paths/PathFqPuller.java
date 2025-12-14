package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fio;
import com.github.kjetilv.uplift.fq.FqPuller;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.atomic.LongAdder;

public class PathFqPuller<T> extends AbstractPathFqReader<T> implements FqPuller<T> {

    private final Collection<Path> processed = new HashSet<>();

    private Puller currentPuller;

    private final boolean deleting;

    private final LongAdder count = new LongAdder();

    public PathFqPuller(Path path, Fio<T> fio, boolean deleting, Charset cs) {
        super(path, fio, cs);
        this.deleting = deleting;
    }

    @Override
    public Optional<T> next() {
        Backoff backoff = null;
        while (true) {
            var nextLine = nextLine(currentPuller);
            if (nextLine.isPresent()) {
                try {
                    return nextLine.map(this::fromString);
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
            currentPuller = nextPuller(processed);
            if (currentPuller == null) {
                if (done()) {
                    return Optional.empty();
                }
                (backoff == null ? backoff = new Backoff() : backoff).zzz();
            }
        }
    }

    private Optional<String> nextLine(Puller puller) {
        return Optional.ofNullable(puller)
            .flatMap(Puller::pull);
    }

    private Puller nextPuller(Collection<Path> processed) {
        return sortedFiles().stream()
            .filter(path ->
                !path.equals(tombstone()))
            .filter(file ->
                !processed.contains(file))
            .findFirst()
            .map(path ->
                new Puller(path, bufferedReader(path)))
            .orElse(null);
    }
}
