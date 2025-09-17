package com.github.kjetilv.uplift.edam.internal;

import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.Hashes;
import org.junit.jupiter.api.Test;

import static com.github.kjetilv.uplift.hash.HashKind.K128;
import static org.junit.jupiter.api.Assertions.*;

class HashesTest {

    @Test
    void testBasicFunctionality() {
        Hasher<Throwable, K128> hasher = new ThrowableHasher<>(
            false,
            Hashes.hashBuilder(K128)
        );
        RuntimeException exception = new RuntimeException("Test exception");
        Hash<K128> hash = hasher.hash(exception);

        assertNotNull(hash, "Hash should not be null");
    }

    @Test
    void testConsistency() {
        Hasher<Throwable, K128> hasher = new ThrowableHasher<>(
            false,
            Hashes.hashBuilder(K128)
        );
        RuntimeException exception = new RuntimeException("Test exception");

        Hash<K128> hash1 = hasher.hash(exception);
        Hash<K128> hash2 = hasher.hash(exception);

        assertEquals(hash1, hash2, "Hashing the same exception twice should produce the same hash");
        assertEquals(hash1.digest(), hash2.digest(), "Hashes should be equal");
        assertArrayEquals(hash1.bytes(), hash2.bytes(), "Hashes should be equal");
    }

    @Test
    void testDifferentExceptions() {
        RuntimeException exception1 = new RuntimeException("Test exception 1");
        RuntimeException exception2 = new RuntimeException("Test exception 2");

        // Different exceptions should produce different hashes when messages are included
        Hasher<Throwable, K128> hasherWithMessages = new ThrowableHasher<>(
            true,
            Hashes.hashBuilder(K128)
        );
        Hash<K128> hash1WithMessages = hasherWithMessages.hash(exception1);
        Hash<K128> hash2WithMessages = hasherWithMessages.hash(exception2);

        assertNotEquals(
            hash1WithMessages,
            hash2WithMessages,
            "Different exceptions with different messages should produce different hashes when messages are included"
        );
    }

    @Test
    void testMessagesOption() {
        RuntimeException exception1 = new RuntimeException("Test exception");
        RuntimeException exception2 = new RuntimeException("Different message");

        // Without messages
        Hash<K128> hash1WithoutMessages = new ThrowableHasher<>(
            false,
            Hashes.hashBuilder(K128)
        ).hash(exception1);
        Hash<K128> hash2WithoutMessages = new ThrowableHasher<>(
            false,
            Hashes.hashBuilder(K128)
        ).hash(exception2);

        // With messages
        Hash<K128> hash1WithMessages = new ThrowableHasher<>(
            false,
            Hashes.hashBuilder(K128)
        ).hash(exception1);
        Hash<K128> hash2WithMessages = new ThrowableHasher<>(
            false,
            Hashes.hashBuilder(K128)
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
            Hashes.hashBuilder(K128)
        );

        // Exception without cause
        RuntimeException exceptionWithoutCause = new RuntimeException("No cause");
        Hash<K128> hashWithoutCause = hasher.hash(exceptionWithoutCause);

        // Exception with cause
        IllegalArgumentException cause = new IllegalArgumentException("The cause");
        RuntimeException exceptionWithCause = new RuntimeException("Has cause", cause);
        Hash<K128> hashWithCause = hasher.hash(exceptionWithCause);

        assertNotEquals(
            hashWithoutCause, hashWithCause,
            "Exception with cause should produce different hash than exception without cause"
        );
    }

    @Test
    void testSuppressedExceptions() {
        Hasher<Throwable, K128> hasher = new ThrowableHasher<>(
            false,
            Hashes.hashBuilder(K128)
        );

        // Exception without suppressed
        RuntimeException exceptionWithoutSuppressed = new RuntimeException("No suppressed");
        Hash<K128> hashWithoutSuppressed = hasher.hash(exceptionWithoutSuppressed);

        // Exception with suppressed
        RuntimeException exceptionWithSuppressed = new RuntimeException("Has suppressed");
        exceptionWithSuppressed.addSuppressed(new IllegalStateException("Suppressed exception"));
        Hash<K128> hashWithSuppressed = hasher.hash(exceptionWithSuppressed);

        assertNotEquals(
            hashWithoutSuppressed, hashWithSuppressed,
            "Exception with suppressed should produce different hash than exception without suppressed"
        );
    }

    @Test
    void testComplexExceptionHierarchy() {
        Hasher<Throwable, K128> hasher = new ThrowableHasher<>(
            true,
            Hashes.hashBuilder(K128)
        );

        // Create a complex exception hierarchy
        IllegalArgumentException deepCause = new IllegalArgumentException("Deep cause");
        RuntimeException middleCause = new RuntimeException("Middle cause", deepCause);
        RuntimeException topException = new RuntimeException("Top exception", middleCause);
        topException.addSuppressed(new IllegalStateException("Suppressed 1"));
        topException.addSuppressed(new NullPointerException("Suppressed 2"));

        Hash<K128> hash = hasher.hash(topException);
        assertNotNull(hash, "Hash should not be null for complex exception hierarchy");

        // Create a slightly different hierarchy
        IllegalArgumentException differentDeepCause = new IllegalArgumentException("Different deep cause");
        RuntimeException differentMiddleCause = new RuntimeException("Middle cause", differentDeepCause);
        RuntimeException differentTopException = new RuntimeException("Top exception", differentMiddleCause);
        differentTopException.addSuppressed(new IllegalStateException("Suppressed 1"));
        differentTopException.addSuppressed(new NullPointerException("Suppressed 2"));

        Hash<K128> differentHash = hasher.hash(differentTopException);
        assertNotEquals(hash, differentHash, "Different exception hierarchies should produce different hashes");
    }
}