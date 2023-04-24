package uplift.kernel;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import uplift.flogs.DefaultLogFormatter;
import org.slf4j.LoggerFactory;

public final class ManagedExecutors {

    public static ExecutorService executor(String prefix) {
        return executor(prefix, CORE_THREADS_DEFAULT);
    }

    public static ExecutorService executor(String prefix, int coreThreads) {
        return executor(prefix, coreThreads, MAX_QUEUE_LENGTH_DEFAULT);
    }

    public static ExecutorService executor(String prefix, int coreThreads, int maxQueueLength) {
        if (coreThreads < 1) {
            throw new IllegalArgumentException("Invalid core threads: " + coreThreads);
        }
        if (maxQueueLength < 1) {
            throw new IllegalArgumentException("Invalid queue length: " + maxQueueLength);
        }
        //         return new ForkJoinPool(coreThreads);
        return new ThreadPoolExecutor(
            coreThreads,
            coreThreads,
            KEEP_ALIVE_TIME.toMillis(),
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(maxQueueLength),
            threadFactory(prefix),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    public static Function<Long, Optional<String>> threadNamer() {
        return id -> Optional.ofNullable(THREAD_NAMES.get(id));
    }

    static {
        THREAD_NAMES = new ConcurrentHashMap<>();
        DefaultLogFormatter.set(() -> new DefaultLogFormatter(threadNamer()));
    }

    private ManagedExecutors() {

    }

    private static final Duration KEEP_ALIVE_TIME = Duration.ofSeconds(20);

    @SuppressWarnings("StaticCollection")
    private static final ConcurrentMap<Long, String> THREAD_NAMES;

    private static final int CORE_THREADS_DEFAULT = 4;

    private static final int MAX_QUEUE_LENGTH_DEFAULT = 10;

    private final static AtomicLong THREAD_COUNT = new AtomicLong();

    private static ThreadFactory threadFactory(String prefix) {
        return run -> {
            String name = prefix + (prefix.endsWith("-") ? "" : "-") + THREAD_COUNT.getAndIncrement();
            Thread thread = new Thread(() -> {
                try {
                    run.run();
                } catch (Throwable e) {
                    LoggerFactory.getLogger(ManagedExecutors.class).warn("{}: Uncaught exception {}", name, run, e);
                }
            }, name);
            THREAD_NAMES.put(thread.threadId(), name);
            return thread;
        };
    }
}
