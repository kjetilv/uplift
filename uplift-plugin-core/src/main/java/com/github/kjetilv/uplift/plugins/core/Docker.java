package com.github.kjetilv.uplift.plugins.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Runs the docker CLI. Replaces Gradle's ExecOperations with ProcessBuilder, so the core
 * is host independent.
 * <p>
 * Output is read and forwarded to {@link Log} rather than inherited. Inheriting would send
 * it to the daemon's own streams, where it is invisible in the build output, and a failing
 * docker build would then report nothing but an exit code.
 */
public final class Docker {

    public static final String BINARY_PROPERTY = "docker.binary";

    public static final String DEFAULT_BINARY = "docker";

    private final String binary;

    private final Log log;

    public Docker(String binary, Log log) {
        this.binary = binary != null && !binary.isBlank() ? binary : DEFAULT_BINARY;
        this.log = log;
    }

    /**
     * The command is split on single spaces, exactly as the Kotlin version did. Templates
     * and callers rely on that, so arguments must not contain spaces.
     */
    public void run(Path cwd, String dockerCommand) {
        String command = binary + " " + dockerCommand;
        List<String> line = Arrays.asList(command.split(" "));
        log.info("Running command in " + cwd);
        log.info("  " + String.join(" ", line));
        try {
            Process process = new ProcessBuilder(line)
                .directory(cwd.toFile())
                .redirectErrorStream(true)
                .start();
            List<String> output = drain(process);
            int exit = process.waitFor();
            if (exit != 0) {
                output.forEach(log::warn);
                throw new IllegalStateException("Exited with code " + exit);
            }
            log.info("Completed: " + command);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(failure(command, cwd), e);
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException(failure(command, cwd), e);
        }
    }

    /**
     * Reads the merged output as it arrives, so the process cannot block on a full pipe.
     * Kept for the failure path, where it is the only diagnostic there is.
     */
    private List<String> drain(Process process) throws IOException {
        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))
        ) {
            return reader.lines()
                .peek(log::info)
                .toList();
        }
    }

    private static String failure(String command, Path cwd) {
        return "Failed to run command: `" + command + "` in " + cwd;
    }
}
