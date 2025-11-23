package com.github.kjetilv.uplift.edam.internal;

import com.github.kjetilv.uplift.hash.HashBuilder;
import org.junit.jupiter.api.Test;

import static com.github.kjetilv.uplift.hash.HashKind.K128;
import static org.junit.jupiter.api.Assertions.*;

class HashesTest {

    @Test
    void testBasicFunctionality() {
        Hasher<Throwable, K128> hasher = new ThrowableHasher<>(
            false,
            HashBuilder.forKind(K128)
        );
        var exception = new RuntimeException("Test exception");
        var hash = hasher.hash(exception);

        assertNotNull(hash, "Hash should not be null");
    }

    @Test
    void testConsistency() {
        Hasher<Throwable, K128> hasher = new ThrowableHasher<>(
            false,
            HashBuilder.forKind(K128)
        );
        var exception = new RuntimeException("Test exception");

        var hash1 = hasher.hash(exception);
        var hash2 = hasher.hash(exception);

        assertEquals(hash1, hash2, "Hashing the same exception twice should produce the same hash");
        assertEquals(hash1.digest(), hash2.digest(), "Hashes should be equal");
        assertArrayEquals(hash1.bytes(), hash2.bytes(), "Hashes should be equal");
    }

    @Test
    void testDifferentExceptions() {
        var exception1 = new RuntimeException("Test exception 1");
        var exception2 = new RuntimeException("Test exception 2");

        // Different exceptions should produce different hashes when messages are included
        Hasher<Throwable, K128> hasherWithMessages = new ThrowableHasher<>(
            true,
            HashBuilder.forKind(K128)
        );
        var hash1WithMessages = hasherWithMessages.hash(exception1);
        var hash2WithMessages = hasherWithMessages.hash(exception2);

        assertNotEquals(
            hash1WithMessages,
            hash2WithMessages,
            "Different exceptions with different messages should produce different hashes when messages are included"
        );
    }

    @Test
    void testMessagesOption() {
        var exception1 = new RuntimeException("Test exception");
        var exception2 = new RuntimeException("Different message");

        // Without messages
        var hash1WithoutMessages = new ThrowableHasher<>(
            false,
            HashBuilder.forKind(K128)
        ).hash(exception1);
        var hash2WithoutMessages = new ThrowableHasher<>(
            false,
            HashBuilder.forKind(K128)
        ).hash(exception2);

        // With messages
        var hash1WithMessages = new ThrowableHasher<>(
            false,
            HashBuilder.forKind(K128)
        ).hash(exception1);
        var hash2WithMessages = new ThrowableHasher<>(
            false,
            HashBuilder.forKind(K128)
        ).hash(exception2);

        // Same exception type with different messages should produce the same hash when messages are not included
        assertNotEquals(
            hash1WithoutMessages,
            hash2WithoutMessages,
            "Same exception type with different messages should produce the same hash when messages are not included"
        );

        // Same exception type with different messages should produce different hashes when messages are included
        assertNotEquals(
            hash1WithMessages, hash2WithMessages,
            "Same exception type with different messages should produce different hashes when messages are included"
        );
    }

    @Test
    void testCauseHandling() {
        Hasher<Throwable, K128> hasher = new ThrowableHasher<>(
            false,
            HashBuilder.forKind(K128)
        );

        // Exception without cause
        var exceptionWithoutCause = new RuntimeException("No cause");
        var hashWithoutCause = hasher.hash(exceptionWithoutCause);

        // Exception with cause
        var cause = new IllegalArgumentException("The cause");
        var exceptionWithCause = new RuntimeException("Has cause", cause);
        var hashWithCause = hasher.hash(exceptionWithCause);

        assertNotEquals(
            hashWithoutCause, hashWithCause,
            "Exception with cause should produce different hash than exception without cause"
        );
    }

    @Test
    void testSuppressedExceptions() {
        Hasher<Throwable, K128> hasher = new ThrowableHasher<>(
            false,
            HashBuilder.forKind(K128)
        );

        // Exception without suppressed
        var exceptionWithoutSuppressed = new RuntimeException("No suppressed");
        var hashWithoutSuppressed = hasher.hash(exceptionWithoutSuppressed);

        // Exception with suppressed
        var exceptionWithSuppressed = new RuntimeException("Has suppressed");
        exceptionWithSuppressed.addSuppressed(new IllegalStateException("Suppressed exception"));
        var hashWithSuppressed = hasher.hash(exceptionWithSuppressed);

        assertNotEquals(
            hashWithoutSuppressed, hashWithSuppressed,
            "Exception with suppressed should produce different hash than exception without suppressed"
        );
    }

    @Test
    void testComplexExceptionHierarchy() {
        Hasher<Throwable, K128> hasher = new ThrowableHasher<>(
            true,
            HashBuilder.forKind(K128)
        );

        // Create a complex exception hierarchy
        var deepCause = new IllegalArgumentException("Deep cause");
        var middleCause = new RuntimeException("Middle cause", deepCause);
        var topException = new RuntimeException("Top exception", middleCause);
        topException.addSuppressed(new IllegalStateException("Suppressed 1"));
        topException.addSuppressed(new NullPointerException("Suppressed 2"));

        var hash = hasher.hash(topException);
        assertNotNull(hash, "Hash should not be null for complex exception hierarchy");

        // Create a slightly different hierarchy
        var differentDeepCause = new IllegalArgumentException("Different deep cause");
        var differentMiddleCause = new RuntimeException("Middle cause", differentDeepCause);
        var differentTopException = new RuntimeException("Top exception", differentMiddleCause);
        differentTopException.addSuppressed(new IllegalStateException("Suppressed 1"));
        differentTopException.addSuppressed(new NullPointerException("Suppressed 2"));

        var differentHash = hasher.hash(differentTopException);
        assertNotEquals(hash, differentHash, "Different exception hierarchies should produce different hashes");
    }
}