package com.github.kjetilv.uplift.flogs.test;

import module java.base;
import com.github.kjetilv.uplift.flogs.Flogs;
import com.github.kjetilv.uplift.flogs.LogLevel;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class FlogsTest {

    @Test
    void test() {
        Flogs.initialize(LogLevel.ERROR);
        var logger = LoggerFactory.getLogger("sdf");
        assertThat(logger).isNotNull();
        logger.info("Quiet here");
    }
}
