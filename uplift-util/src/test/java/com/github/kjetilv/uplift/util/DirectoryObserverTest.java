package com.github.kjetilv.uplift.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class DirectoryObserverTest {

    static void main(String[] args) throws IOException {
        var tmp = tmp();
        var list = Files.list(tmp).toList();
        System.out.println("Going in: " + list);
        var observer = new DirectoryObserver(tmp, list);
        CompletableFuture.runAsync(() -> {
            try {
                new BufferedReader(new InputStreamReader(System.in)).readLine();
                System.out.println("Closing");
                observer.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        var update = observer.awaitChange(Duration.ofHours(1));
        System.out.println(update);

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
