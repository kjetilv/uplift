package com.github.kjetilv.uplift.edam.ex;

import com.github.kjetilv.uplift.edam.Analysis;
import com.github.kjetilv.uplift.edam.Window;
import com.github.kjetilv.uplift.edam.throwables.ThrowableInfo;
import com.github.kjetilv.uplift.edam.throwables.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.IntConsumer;
import java.util.stream.Stream;

import static com.github.kjetilv.uplift.hash.HashKind.K256;

public final class Main {

    private static final Logger log = LoggerFactory.getLogger("Main");

    public static final Set<Integer> NO_DIVS = Set.of(3, 7, 9, 11, 13, 17, 19, 21, 23);

    static void main() {
        var window = new Window(Duration.ofMinutes(1), 128);
        try (var arena = Arena.ofConfined()) {
            var handler = Throwables.offHeap(
                arena,
                window,
                K256,
                4,
                true
            );
            var intConsumer = intConsumer();
            Stream.of(
                    1,
                    2,
                    3,
                    4,
                    5,
                    null,
                    null,
                    0,
                    null,
                    6,
                    0,
                    null,
                    null,
                    0,
                    null,
                    7,
                    null,
                    0,
                    null,
                    7,
                    null,
                    0,
                    null,
                    7,
                    null,
                    0,
                    null,
                    7,
                    null,
                    0,
                    0,
                    0,
                    0,
                    0,
                    null,
                    9,
                    null,
                    null,
                    2,
                    null,
                    null,
                    2,
                    null,
                    null,
                    11,
                    11,
                    2,
                    8,
                    null
                )
                .forEach(i -> {
                    try {
                        process(i, intConsumer);
                    } catch (Exception e) {
                        handler.handle(
                            e, (analysis, info) -> {
                                log.info(
                                    "Handled {} lines, {} chars: {}",
                                    info.lines(),
                                    info.volume(),
                                    info.source().toString()
                                );
                                if (analysis instanceof Analysis.Multiple<?> multiple) {
                                    log.info(
                                        "{} {}\n  {} time{} in {} pattern{} in the last {}: {}",
                                        info.causeChain(),
                                        analysis.triggerHash(),
                                        analysis.triggerHashCount(),
                                        analysis.triggerHashCount() > 1 ? "s" : "",
                                        analysis.distinctPatternsCount(),
                                        analysis.distinctPatternsCount() > 1 ? "s" : "",
                                        analysis.duration().truncatedTo(ChronoUnit.MILLIS),
                                        analysis.toPatternMatchesString()
                                    );
                                    logSimple(multiple.simple(), info);
                                } else {
                                    logSimple(analysis, info);
                                }
                            }
                        );
                    }
                });
        }
    }

    private static void logSimple(Analysis<?> analysis, ThrowableInfo<K256> info) {
        var brief = analysis.triggerHashCount() > 2;
        var hash = analysis.triggerHash();
        log.warn(
            "{} {}\n {} {}",
            info.causeChain(),
            hash.toLongString(),
            brief
                ? hash.toString()
                : hash.toShortString(),
            String.join("\n", brief ? info.brief() : info.full())
        );
    }

    private static void process(Integer i, IntConsumer intConsumer) {
        try {
            intConsumer.accept(i);
        } catch (Exception e) {
            throw new RuntimeException(i + " failed", e);
        }
    }

    private static IntConsumer intConsumer() {
        var da = new DoubleAdder();
        return i -> {
            log.info("Processing {}", i);
            if (i == 0) {
                throw new IllegalArgumentException("i == 0");
            }
            if (NO_DIVS.contains(odd(i))) {
                throw new IllegalStateException("No division by " + (Integer) i);
            }
            da.add(1.0 / i);
            log.info("Currently at {}", da.doubleValue());
        };
    }

    private static int odd(int i) {
        return i % 2 == 0
            ? odd(i / 2)
            : i;
    }
}