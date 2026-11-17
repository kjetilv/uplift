module uplift.flogs {
    requires java.logging;

    // For slf4j users
    exports org.slf4j;
    exports org.slf4j.event;
    exports org.slf4j.helpers;
    exports org.slf4j.spi;

    // For org.apache.commons users
    exports org.apache.commons.logging;

    // For logj4 users
    exports org.apache.logging.log4j;
    exports org.apache.logging.log4j.message;
    exports org.apache.logging.log4j.util;

    // For the floggers
    exports com.github.kjetilv.uplift.flogs;

    provides java.lang.System.LoggerFinder with com.github.kjetilv.uplift.flogs.FloggerFinder;
}
