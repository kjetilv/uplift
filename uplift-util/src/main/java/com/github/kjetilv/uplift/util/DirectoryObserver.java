package com.github.kjetilv.uplift.util;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.time.Instant.now;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public final class DirectoryObserver implements Closeable {

    public static final int MAX_WAIT_TIME = 1000;

    private final Path root;

    private final Set<Path> paths;

    private final WatchService service;

    private final AtomicLong waitTime = new AtomicLong(10);

    public DirectoryObserver(Path root, List<Path> lastSeen) {
        this.root = Objects.requireNonNull(root, "directory");
        this.paths = Set.copyOf(lastSeen);
        try {
            this.service = FileSystems.getDefault().newWatchService();
            this.root.register(this.service, ENTRY_CREATE);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to setup watch service", e);
        }
    }

    public Optional<Update> awaitChange(Duration timeout) {
        var initialUpdate = update();
        if (initialUpdate.changed()) {
            return Optional.of(initialUpdate);
        }
        var deadline = now().plus(timeout);
        while (now().isBefore(deadline)) {
            WatchKey key;
            try {
                key = service.poll(backoffTime(), MILLISECONDS);
            } catch (ClosedWatchServiceException e) {
                return Optional.empty();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted", e);
            }
            if (key == null) {
                continue;
            }
            List<WatchEvent<?>> events;
            try {
                events = key.pollEvents();
                if (events.isEmpty()) {
                    continue;
                }
            } finally {
                key.reset();
            }
            var update = update();
            if (update.changed()) {
                return Optional.of(update);
            }
        }
        return Optional.empty();
    }

    @Override
    public void close() throws IOException {
        service.close();
    }

    private long backoffTime() {
        return waitTime.getAndUpdate(t -> Math.min(MAX_WAIT_TIME, t * 2));
    }

    private Update update() {
        try (var currentStream = Files.list(root)) {
            var current = currentStream.collect(Collectors.toSet());
            if (current.equals(paths)) {
                return new NoChange();
            }
            if (current.containsAll(paths)) {
                var added =
                    sort(current.stream()
                        .filter(fresh -> !paths.contains(fresh)));
                return new Added(added);
            }
            return new Changed(sort(current.stream()));
        } catch (Exception e) {
            throw new IllegalStateException("Could not list files in " + root, e);
        }
    }

    private static final String EMPTY_STRING = "[]";

    private static final Comparator<Path> BY_NAME = Comparator.comparing(
        path -> path.getFileName().toString(),
        CASE_INSENSITIVE_ORDER
    );

    private static List<Path> sort(Stream<Path> stream) {
        return stream.sorted(BY_NAME)
            .toList();
    }

    private static String print(List<Path> paths) {
        if (paths.isEmpty()) {
            return EMPTY_STRING;
        }
        return paths.stream()
            .map(Path::getFileName)
            .map(Path::toString)
            .collect(Collectors.joining(", ", "[", "]"));
    }

    public record Added(List<Path> added) implements Update {

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + added.size() + ": " + print(added) + "]";
        }
    }

    public record Changed(List<Path> current) implements Update {

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + current.size() + ": " + print(current) + "]";
        }
    }

    public record NoChange() implements Update {

        @Override
        public boolean changed() {
            return false;
        }
    }

    public sealed interface Update {

        default boolean changed() {
            return true;
        }
    }
}
