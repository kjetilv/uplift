package com.github.kjetilv.uplift.util;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.time.Instant.now;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public final class DirectoryObserver implements Closeable {

    private final Path root;

    private final Set<Path> paths;

    private final WatchService watchService;

    private final AtomicLong waitTime = new AtomicLong(10);

    public DirectoryObserver(Path root, List<Path> lastSeen) {
        this.root = Objects.requireNonNull(root, "directory");
        this.paths = lastSeen == null || lastSeen.isEmpty()
            ? Set.of()
            : Set.copyOf(lastSeen);
        try {
            this.watchService = FileSystems.getDefault().newWatchService();
            this.root.register(watchService, ENTRY_CREATE);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to setup watch service", e);
        }
    }

    public Update awaitChange(Duration timeout) {
        var initialUpdate = update();
        if (initialUpdate.changed()) {
            return update();
        }
        var deadline = now().plus(timeout);
        do {
            var key = poll(increasingWaitTime());
            if (key != null) {
                key.pollEvents();
                key.reset();
            }
            var update = update();
            if (update.changed()) {
                return update;
            }
        } while (now().isBefore(deadline));
        return NO_CHANGE;
    }

    @Override
    public void close() throws IOException {
        watchService.close();
    }

    private WatchKey poll(long ms) {
        try {
            return watchService.poll(ms, MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted", e);
        }
    }

    private long increasingWaitTime() {
        return waitTime.getAndUpdate(t -> Math.min(1000, Math.round(t * 2)));
    }

    private Update update() {
        try (
            var currentStream = Files.list(root)
        ) {
            var current = currentStream.collect(Collectors.toSet());
            if (current.equals(paths)) {
                return NO_CHANGE;
            }
            if (current.isEmpty()) {
                return EMPTY;
            }
            if (current.containsAll(paths)) {
                var added =
                    sort(current.stream().filter(fresh -> !paths.contains(fresh)));
                return new Added(added);
            }
            return new Changed(sort(current.stream()));
        } catch (Exception e) {
            throw new IllegalStateException("Could not list files in " + root, e);
        }
    }

    private static final NoChange NO_CHANGE = new NoChange();

    private static final Empty EMPTY = new Empty();

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
        return paths.isEmpty()
            ? EMPTY_STRING
            : paths.stream()
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

    public record Empty() implements Update {

        @Override
        public String toString() {
            return getClass().getSimpleName() + EMPTY_STRING;
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
