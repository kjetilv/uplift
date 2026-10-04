package com.github.kjetilv.uplift.util;

import module java.base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.kjetilv.uplift.util.ScriptRun.LineListener.Result.DONE;

class ScriptRun {

    private static final Logger log = LoggerFactory.getLogger(ScriptRun.class);

    public static ScriptRun.Builder build(Path path) {
        if (Files.isExecutable(path)) {
            return new BuilderImpl(path);
        }
        throw new IllegalArgumentException("Not an executable: " + path);
    }

    boolean completed;

    private final List<LineListener> listeners;

    private final Path path;

    private final ProcessBuilder processBuilder;

    private final String[] command;

    private final Process process;

    private final CompletableFuture<Integer> stdout;

    private final CompletableFuture<Integer> stderr;

    private final CountDownLatch inputLatch = new CountDownLatch(2);

    ScriptRun(
        Path path,
        ProcessBuilder processBuilder,
        List<LineListener> listeners,
        String... command
    ) {
        this.path = path;
        this.processBuilder = processBuilder;
        this.listeners = listeners;
        this.command = command.clone();
        try {
            process = processBuilder.start();
        } catch (Exception e) {
            throw new IllegalStateException(this + " could not start process", e);
        }
        stdout = run(listeners, "stdout", process.getInputStream(), LineListener::stdout);
        stderr = run(listeners, "stderr", process.getErrorStream(), LineListener::stderr);
    }

    private CompletableFuture<Integer> run(
        List<LineListener> listeners,
        String stream,
        InputStream inputStream,
        BiFunction<LineListener, String, LineListener.Result> processor
    ) {
        CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
        var thread = Thread.ofVirtual().name(stream).start(() -> {
            int lines;
            try (
                var reader = new BufferedReader(new InputStreamReader(inputStream))
            ) {
                inputLatch.countDown();
                lines = run(reader, listeners, processor);
            } catch (Exception e) {
                throw new IllegalStateException(this + " failed to listen to " + stream, e);
            }
            completableFuture.complete(lines);
        });
        ScriptRun.log.info("{}: Started {} thread {}", this, stream, thread);
        return completableFuture;
    }

    private int run(
        BufferedReader reader,
        List<LineListener> listeners,
        BiFunction<LineListener, String, LineListener.Result> processor
    ) {
        String line;
        int lines = 0;
        while (true) {
            try {
                if ((line = reader.readLine()) == null) {
                    return lines;
                }
            } catch (Exception e) {
                throw new IllegalStateException("Failed to read", e);
            }
            lines++;
            var iterator = listeners.listIterator();
            while (iterator.hasNext()) {
                var next = iterator.next();
                var result = processor.apply(next, line);
                if (result == DONE) {
                    iterator.remove();
                }
            }
        }
    }

    private void await(boolean kill, Duration timeout) {
        var awaitStart = Instant.now();
        boolean await;
        try {
            await = inputLatch.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for start", e);
        }
        if (!await) {
            throw new IllegalStateException("Timed out waiting for start");
        }
        var remainingTimeout = timeout.minus(Duration.between(awaitStart, Instant.now()));
        if (!kill || killed(3)) {
            boolean completed;
            try {
                completed = process.waitFor(remainingTimeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for completion", e);
            }
            if (!completed) {
                throw new IllegalStateException("Timed out waiting for completion");
            }
        } else {
            log.warn("Process not terminated");
        }
    }

    private boolean killed(int retries) {
        var count = 0;
        for (; count < retries && process.isAlive(); count++) {
            process.destroy();
            process.descendants()
                .forEach(ProcessHandle::destroy);
        }
        if (!process.isAlive()) {
            log.info(
                "Process killed{}",
                count > 0 ? ", " + count + " retries" : ""
            );
            return true;
        }
        var forceCounts = 0;
        for (; forceCounts < retries && process.isAlive(); forceCounts++) {
            process.destroyForcibly();
            process.descendants()
                .forEach(ProcessHandle::destroyForcibly);
        }
        if (!process.isAlive()) {
            log.info(
                "Process forcibly killed{}, after {} kill attempts",
                forceCounts > 0 ? ", " + forceCounts + " retries" : "",
                count
            );
            return true;
        }
        log.warn("Process still alive: {}", process);
        return false;
    }

    public interface Builder {

        ScriptRun.Builder command(String... args);

        ScriptRun.Builder cwd(Path path);

        ScriptRun.Builder listen(LineListener... listeners);

        ScriptRun run();

    }

    public interface LineListener {

        default Result stderr(String line) {
            return stdout(line);
        }

        Result stdout(String line);

        public enum Result {
            DONE,
            CONTINUE
        }
    }
}
