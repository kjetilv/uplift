package com.github.kjetilv.uplift.edam.internal;

import com.github.kjetilv.uplift.edam.Analysis;
import com.github.kjetilv.uplift.edam.Handler;
import com.github.kjetilv.uplift.edam.Handling;
import com.github.kjetilv.uplift.edam.Window;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.edam.patterns.PatternMatch;
import com.github.kjetilv.uplift.edam.throwables.ThrowableInfo;
import com.github.kjetilv.uplift.edam.throwables.Throwables;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
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

class DefaultHandlerTest {

    private final AtomicReference<Instant> now = new AtomicReference<>(Instant.ofEpochMilli(0));

    private final Hasher<Throwable, K128> throwableHasher = new ThrowableHasher<>(
        false,
        HashBuilder.forKind(K128)
    );

    private Handler<Throwable, ThrowableInfo<K128>, K128> handler;

    private final RuntimeException re0 = new RuntimeException("a");

    private final Hash<K128> hash0 = throwableHasher.hash(re0);

    private final RuntimeException re1 = new RuntimeException("b");

    private final Hash<K128> hash1 = throwableHasher.hash(re1);

    private final RuntimeException re2 = new RuntimeException("c");

    private final Hash<K128> hash2 = throwableHasher.hash(re2);

    private final RuntimeException re3 = new RuntimeException("d");

    private Arena arena;

    @BeforeEach
    void setUp() {
        arena = Arena.ofConfined();
        var window = new Window(Duration.ofSeconds(30), 10);
        handler = Throwables.offHeap(
            arena,
            now::get,
            window,
            K128,
            null,
            null,
            0,
            false
        );
    }

    @AfterEach
    void tearDown() {
        arena.close();
    }

    @Test
    void subsumed() {
        result(re0);
        result(re1);
        result(re2);
        result(re3);
        result(re0);
        result(re1);
        var handling = result(re2);

        if (handling.analysis() instanceof Analysis.Multiple(
            Occurrence<K128> occurred,
            List<PatternMatch<K128>> sequenceOccurrences
        )) {
            assertEquals(hash2, occurred.hash());
            assertEquals(3, sequenceOccurrences.size());
        } else {
            fail(handling + " is not instance of " + Analysis.Multiple.class.getSimpleName());
        }
    }

    @Test
    void sdfdsf() {
        var res1 = result(re0);
        assertInstanceOf(
            Analysis.None.class,
            res1.analysis(),
            () -> "? " + res1
        );
        var handle2 = result(re1); // ab
        assertInstanceOf(
            Analysis.None.class,
            handle2.analysis(),
            () -> "? " + handle2
        );
        var handle3 = result(re2); // abc
        assertInstanceOf(
            Analysis.None.class,
            handle3.analysis(),
            () -> "? " + handle3
        );

        var handle4 = result(re1); // abcb
        if (handle4.analysis() instanceof Analysis.Simple<K128>(var times)) {
            assertEquals(hash1, handle4.analysis().trigger().hash());
            assertEquals(2, times.size());
        } else {
            fail(handle4 + " is not instance of " + Analysis.Multiple.class.getSimpleName());
        }

        var handle5 = result(re2); // abcbc
        if (handle5.analysis() instanceof Analysis.Multiple<K128> repeated) {
            assertEquals(
                2,
                repeated.matches().size(),
                () -> "Missing patterns? " + handle5
            );
            assertEquals(2, repeated.occurrences(hash2).size());
            assertEquals(2, repeated.occurrences(hash1, hash2).size());
        } else {
            fail(handle5 + " is not instance of " + Analysis.Multiple.class.getSimpleName());
        }

        var handle6 = result(re1); // abcbcb
        if (handle6.analysis() instanceof Analysis.Multiple<K128> repeated) { // b*3, cb*2
            assertEquals(2, repeated.matches().size(), () -> "Missing patterns? " + handle6);
            assertEquals(3, repeated.occurrences(hash1).size());
            assertEquals(2, repeated.occurrences(hash2, hash1).size());
        } else {
            fail(handle6 + " is not instance of " + Analysis.Multiple.class.getSimpleName());
        }

        var handle7 = result(re2); // abcbcbc
        if (handle7.analysis() instanceof Analysis.Multiple<K128> repeated) { // bc*3, c*3
            assertEquals(2, repeated.matches().size());
            assertEquals(3, repeated.occurrences(hash1, hash2).size());
            assertEquals(3, repeated.occurrences(hash2).size());
        } else {
            fail(handle7 + " is not instance of " + Analysis.Multiple.class.getSimpleName());
        }

        var handle8 = result(re3); // abcbcbcd
        assertInstanceOf(
            Analysis.None.class,
            handle8.analysis(),
            () -> "? " + handle8
        );

        var handle9 = result(re0);
        if (handle9.analysis() instanceof Analysis.Simple<K128>(var times)) {
            assertEquals(hash0, handle9.analysis().trigger().hash());
            assertEquals(2, times.size());
        } else {
            fail(handle9 + " is not instance of " + Analysis.Multiple.class.getSimpleName());
        }

        var handle10 = result(re0);
        if (handle10.analysis() instanceof Analysis.Simple<K128>(var times)) {
            assertEquals(hash0, handle10.analysis().trigger().hash());
            assertEquals(3, times.size());
        } else {
            fail(handle10 + " is not instance of " + Analysis.Multiple.class.getSimpleName());
        }

        tick(300);
        var handleX = handler.handling(re0); // a
        assertInstanceOf(
            Analysis.None.class,
            handleX.analysis(),
            () -> "? " + handleX
        );
    }

    private Handling<Throwable, ThrowableInfo<K128>, K128> result(RuntimeException e) {
        tick();
        // a
        return handler.handling(e);
    }

    private void tick() {
        tick(1);
    }

    private void tick(int seconds) {
        now.set(now.get().plusSeconds(seconds));
    }
}