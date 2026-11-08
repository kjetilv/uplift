package com.github.kjetilv.uplift.flogs.test;

import module java.base;
import com.github.kjetilv.uplift.flogs.FloggerFinder;
import com.github.kjetilv.uplift.flogs.LogLevel;
import org.junit.jupiter.api.Test;

import static java.lang.System.Logger.Level.*;
import static org.assertj.core.api.Assertions.assertThat;

/// [FloggerFinder] follows the [System.Logger] contract: resource bundles, [MessageFormat] parameters, and
/// `OFF` never being loggable.
class SystemLoggerTest {

    @Test
    void isTheServiceLoadedFinder() {
        assertThat(System.LoggerFinder.getLoggerFinder()).isInstanceOf(FloggerFinder.class);
    }

    @Test
    void formatsParametersWithMessageFormat() {
        var capture = new Capture(LogLevel.TRACE);
        logger(capture).log(INFO, "a {0} b {1}", "x", 2);
        assertThat(capture.calls).containsExactly(new Capture.Call(LogLevel.INFO, "a x b 2", List.of()));
    }

    @Test
    void leavesMessagesWithoutParameterReferencesAsTheyAre() {
        var capture = new Capture(LogLevel.TRACE);
        logger(capture).log(INFO, "it's {} here", "x");
        logger(capture).log(INFO, "bad {0 pattern", "x");
        logger(capture).log(INFO, "no params {0}");
        assertThat(capture.calls).extracting(Capture.Call::message)
            .containsExactly("it's {} here", "bad {0 pattern", "no params {0}");
        assertThat(capture.calls).allSatisfy(call -> assertThat(call.args()).isEmpty());
    }

    @Test
    void localizesWithResourceBundle() {
        var capture = new Capture(LogLevel.TRACE);
        var logger = logger(capture);
        logger.log(INFO, BUNDLE, "greeting", "you");
        logger.log(INFO, BUNDLE, "missing", "you");
        logger.log(WARNING, BUNDLE, "greeting", (Throwable) null);
        assertThat(capture.calls).extracting(Capture.Call::message)
            .containsExactly("hello you", "missing", "hello {0}");
    }

    @Test
    void passesThrowables() {
        var capture = new Capture(LogLevel.TRACE);
        var exception = new IllegalStateException("boom");
        logger(capture).log(ERROR, "failed {}", exception);
        logger(capture).log(ERROR, "no throwable {}", (Throwable) null);
        assertThat(capture.calls).containsExactly(
            new Capture.Call(LogLevel.ERROR, "failed {}", List.of(exception)),
            new Capture.Call(LogLevel.ERROR, "no throwable {}", List.of())
        );
    }

    @Test
    void mapsLevels() {
        var capture = new Capture(LogLevel.TRACE);
        var logger = logger(capture);
        Stream.of(TRACE, DEBUG, INFO, WARNING, ERROR).forEach(level -> logger.log(level, "m"));
        assertThat(capture.calls).extracting(Capture.Call::level).containsExactly(
            LogLevel.TRACE, LogLevel.DEBUG, LogLevel.INFO, LogLevel.WARN, LogLevel.ERROR
        );
    }

    @Test
    void neverLogsAtOff() {
        var capture = new Capture(LogLevel.TRACE);
        var logger = logger(capture);
        assertThat(logger.isLoggable(OFF)).isFalse();
        logger.log(OFF, "m");
        logger.log(OFF, "m {0}", "x");
        logger.log(OFF, "m", new IllegalStateException());
        assertThat(capture.calls).isEmpty();
    }

    @Test
    void skipsDisabledLevelsWithoutFormatting() {
        var capture = new Capture(LogLevel.INFO);
        var logger = logger(capture);
        assertThat(logger.isLoggable(DEBUG)).isFalse();
        logger.log(DEBUG, () -> {
            throw new AssertionError("Supplier called for disabled level");
        });
        logger.log(DEBUG, "m {0}", "x");
        assertThat(capture.calls).isEmpty();
    }

    private static System.Logger logger(Capture capture) {
        return new FloggerFinder(_ -> capture).getLogger("test", SystemLoggerTest.class.getModule());
    }

    private static final ResourceBundle BUNDLE = new ListResourceBundle() {
        @Override
        protected Object[][] getContents() {
            return new Object[][] {{"greeting", "hello {0}"}};
        }
    };
}
