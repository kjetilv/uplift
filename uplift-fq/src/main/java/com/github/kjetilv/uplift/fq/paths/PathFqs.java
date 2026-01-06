package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.*;
import com.github.kjetilv.uplift.fq.flows.Name;
import com.github.kjetilv.uplift.util.SayFiles;
import com.github.kjetilv.uplift.util.Sleeper;

import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.kjetilv.uplift.util.SayFiles.couldCreate;
import static java.nio.file.Files.*;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public final class PathFqs<I, O> implements Fqs<O> {

    public static <I, O> PathFqs<I, O> create(
        Path directory,
        Function<I, O> read,
        Function<O, I> write,
        AccessProvider<Path, I> accessProvider,
        Dimensions dimensions
    ) {
        return create(
            directory,
            fio(
                read,
                write
            ),
            accessProvider,
            dimensions
        );
    }

    public static <I, O> PathFqs<I, O> create(
        Path directory,
        Fio<I, O> fio,
        AccessProvider<Path, I> accessProvider,
        Dimensions dimensions
    ) {
        return new PathFqs<>(
            fio,
            new PathProvider(directory, suffix(directory)),
            accessProvider,
            dimensions
        );
    }

    static Path ensureWritable(Path directory) {
        requireNonNull(directory, "directory");

        if (SayFiles.nonDirectory(directory)) {
            throw new IllegalArgumentException("Path was not a directory: " + directory);
        }

        if (exists(directory) && !isWritable(directory)) {
            throw new IllegalArgumentException("Directory was not writable: " + directory);
        }

        if (!exists(directory) && !couldCreate(directory)) {
            throw new IllegalArgumentException("Directory could not be created: " + directory);
        }

        return directory;
    }

    private final Fio<I, O> fio;

    private final Dimensions dimensions;

    private final AccessProvider<Path, I> accessProvider;

    private final SourceProvider<Path> sourceProvider;

    private final ConcurrentMap<Name, DirSynch> filesCreated = new ConcurrentHashMap<>();

    public PathFqs(
        Fio<I, O> fio,
        SourceProvider<Path> sourceProvider,
        AccessProvider<Path, I> accessProvider,
        Dimensions dimensions
    ) {
        this.fio = requireNonNull(fio, "fio");
        this.sourceProvider = requireNonNull(sourceProvider, "sourceProvider");
        this.accessProvider = requireNonNull(accessProvider, "factories");
        this.dimensions = requireNonNull(dimensions, "dimensions");
    }

    @Override
    public FqReader<O> reader(Name name) {
        var path = sourceProvider.source(name);
        if (isRegularFile(path)) {
            return new FileFqReader<>(
                fio,
                accessProvider.reader(path)
            );
        }
        return new PathFqReader<>(
            path,
            fio,
            accessProvider::reader,
            filesCreated.computeIfAbsent(name, DirSynch::new)::awaitNext,
            new PathTombstone(path.resolve("done")),
            false
        );
    }

    @Override
    public FqWriter<O> writer(Name name) {
        var directory = sourceProvider.source(name);
        return new PathFqWriter<>(
            directory,
            dimensions,
            accessProvider::writer,
            filesCreated.computeIfAbsent(name, DirSynch::new)::next,
            fio,
            new PathTombstone(directory.resolve("done"))
        );
    }

    @Override
    public void init(Name... names) {
        for (Name name : names) {
            ensureWritable(sourceProvider.source(name));
        }
    }

    private static String suffix(Path directory) {
        var fileName = directory.getFileName().toString();
        var index = fileName.lastIndexOf('.');
        var suffix = index == -1 ? null : fileName.substring(index + 1);
        return suffix;
    }

    private static <I, O> Fio<I, O> fio(
        Function<I, O> read,
        Function<O, I> write
    ) {
        return new Fio<>() {

            @Override
            public O read(I line) {
                return read.apply(line);
            }

            @Override
            public I write(O value) {
                return write.apply(value);
            }
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void waitFor(ReentrantLock lock, Condition condition, long ms) {
        lock.lock();
        try {
            condition.await(ms, MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted", e);
        } finally {
            lock.unlock();
        }
    }

    private record DirSynch(Name name, Lock lock, Condition condition, Supplier<Sleeper> sleeper) {

        private DirSynch(Name name) {
            var lock = new ReentrantLock();
            var condition = lock.newCondition();
            this(
                name, lock, condition, Sleeper.deferred(
                    () -> "Wait for " + name,
                    (long ms) -> waitFor(lock, condition, ms)
                )
            );
        }

        private void awaitNext() {
            sleeper.get().sleep();
        }

        private void next() {
            lock.lock();
            try {
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }
}
