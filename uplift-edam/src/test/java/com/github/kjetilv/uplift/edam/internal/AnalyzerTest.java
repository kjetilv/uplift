package com.github.kjetilv.uplift.edam.internal;

import com.github.kjetilv.uplift.edam.Analysis;
import com.github.kjetilv.uplift.edam.Window;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind.K128;
import com.github.kjetilv.uplift.hash.Hashes;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.edam.patterns.PatternMatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.kjetilv.uplift.hash.HashKind.K128;
import static org.junit.jupiter.api.Assertions.*;

class AnalyzerTest {

    private final AtomicReference<Instant> now = new AtomicReference<>(Instant.ofEpochMilli(0));

    private final Hasher<Throwable, K128> throwableHasher = new ThrowableHasher<>(false, Hashes.hashBuilder(
        K128));

    private Analyzer<Throwable, K128> repeatAnalyzer;

    private Arena arena;

    private final RuntimeException re0 = new RuntimeException("a");

    private final Hash<K128> hash0 = throwableHasher.hash(re0);

    private final RuntimeException re1 = new RuntimeException("b");

    private final Hash<K128> hash1 = throwableHasher.hash(re1);

    private final RuntimeException re2 = new RuntimeException("c");

    private final Hash<K128> hash2 = throwableHasher.hash(re2);

    private final RuntimeException re3 = new RuntimeException("d");

    @BeforeEach
    void setUp() {
        arena = Arena.ofConfined();
        Window window = new Window(Duration.ofSeconds(30), 10);
        OffHeapIndexer128 indexer = new OffHeapIndexer128(
            arena, InternalFactory.JAVA, window.count()
        );
        Storage<K128> storage = OffHeapStorage.create(window, indexer, arena);
        Hasher<Throwable, K128> hasher = new ThrowableHasher<>(
            false,
            Hashes.hashBuilder(K128)
        );
        repeatAnalyzer = new Analyzer<>(hasher, storage, now::get, 0);
    }

    @AfterEach
    void tearDown() {
        arena.close();
    }

    @Test
    void analyze() {
        analyze(re0);
        analyze(re1);
        analyze(re2);
        analyze(re3);
        analyze(re0);
        analyze(re1);
        Analysis<K128> analyis = analyze(re2);

        if (analyis instanceof Analysis.Multiple(
            Occurrence<K128> occurred,
            List<PatternMatch<K128>> patternMatches
        )) {
            assertEquals(hash2, occurred.hash());
            assertEquals(3, patternMatches.size());
        } else {
            fail(analyis + " is not instance of " + Analysis.Multiple.class.getSimpleName());
        }
    }

    @Test
    void sdfdsf() {
        Analysis<K128> an1 = analyze(re0);
        assertInstanceOf(
            Analysis.None.class,
            an1,
            () -> "? " + an1
        );
        Analysis<K128> an2 = analyze(re1); // ab
        assertInstanceOf(
            Analysis.None.class,
            an2,
            () -> "? " + an2
        );
        Analysis<K128> an3 = analyze(re2); // abc
        assertInstanceOf(
            Analysis.None.class,
            an3,
            () -> "? " + an3
        );

        Analysis<K128> an4 = analyze(re1); // abcb
        if (an4 instanceof Analysis.Simple<K128>(List<Occurrence<K128>> times)) {
            assertEquals(hash1, an4.trigger().hash());
            assertEquals(2, times.size());
        } else {
            fail(an4 + " is not instance of " + Analysis.Multiple.class.getSimpleName());
        }

        Analysis<K128> an5 = analyze(re2); // abcbc
        if (an5 instanceof Analysis.Multiple<K128> repeated) {
            assertEquals(
                2,
                repeated.matches().size(),
                () -> "Missing patterns? " + an5
            );
            assertEquals(2, repeated.occurrences(hash1, hash2).size());
            assertEquals(2, repeated.occurrences(hash2).size());
        } else {
            fail(an5 + " is not instance of " + Analysis.Multiple.class.getSimpleName());
        }

        Analysis<K128> an6 = analyze(re1); // abcbcb
        if (an6 instanceof Analysis.Multiple<K128> repeated) { // b, cb
            assertEquals(2, repeated.matches().size(), () -> "Missing patterns? " + an6);
            assertEquals(2, repeated.occurrences(hash2, hash1).size());
            assertEquals(3, repeated.occurrences(hash1).size());
        } else {
            fail(an6 + " is not instance of " + Analysis.Multiple.class.getSimpleName());
        }

        Analysis<K128> an7 = analyze(re2); // abcbcbc
        if (an7 instanceof Analysis.Multiple<K128> repeated) {
            assertEquals(2, repeated.matches().size());
            assertEquals(3, repeated.occurrences(hash1, hash2).size());
            assertEquals(3, repeated.occurrences(hash2).size());
        } else {
            fail(an7 + " is not instance of " + Analysis.Multiple.class.getSimpleName());
        }

        Analysis<K128> an8 = analyze(re3); // abcbcbcd
        assertInstanceOf(
            Analysis.None.class,
            an8,
            () -> "? " + an8
        );

        Analysis<K128> an9 = analyze(re0);
        if (an9 instanceof Analysis.Simple<K128>(List<Occurrence<K128>> times)) {
            assertEquals(hash0, an9.trigger().hash());
            assertEquals(2, times.size());
        } else {
            fail(an9 + " is not instance of " + Analysis.Multiple.class.getSimpleName());
        }

        Analysis<K128> an10 = analyze(re0);
        if (an10 instanceof Analysis.Simple<K128>(List<Occurrence<K128>> times)) {
            assertEquals(hash0, an10.trigger().hash());
            assertEquals(3, times.size());
        } else {
            fail(an10 + " is not instance of " + Analysis.Multiple.class.getSimpleName());
        }

        tick(300);
        Analysis<K128> anX = repeatAnalyzer.analyze(re0); // a
        assertInstanceOf(
            Analysis.None.class,
            anX,
            () -> "? " + anX
        );
    }

    private Analysis<K128> analyze(RuntimeException e) {
        tick();
        // a
        return repeatAnalyzer.analyze(e);
    }

    private void tick() {
        tick(1);
    }

    private void tick(int seconds) {
        now.set(now.get().plusSeconds(seconds));
    }
}