package com.github.kjetilv.uplift.util;

import com.github.kjetilv.uplift.util.DirectoryObserver.FileState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DirectoryObserverTest {

    static void main(String[] args) throws IOException {
        var tmp = tmp();
        try (var fileStream = Files.list(tmp)) {
            var files = fileStream.toList();
            System.out.println("Going in: " + files);
            Optional<FileState> update;
            try (var observer = new DirectoryObserver(tmp)) {
                CompletableFuture.runAsync(() -> bg(observer));
                update = observer.awaitChange(files, Duration.ofHours(1));
            }
            update.ifPresentOrElse(System.out::println, () -> System.out.println("N/A"));
        }

    }

    private static void bg(DirectoryObserver observer) {
        try {
            new BufferedReader(new InputStreamReader(System.in)).readLine();
            System.out.println("Closing");
            observer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static java.nio.file.Path tmp() {
        var tmp = Path.of("tmp").toAbsolutePath();
        try {
            Files.createDirectories(tmp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return tmp;
    }
}
