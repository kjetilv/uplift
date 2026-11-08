import com.github.kjetilv.uplift.flogs.FloggerFinder;

/// @jenesis.release 25
module uplift.flogs {
    requires java.logging;

    exports org.slf4j;
    exports org.slf4j.event;
    exports org.slf4j.helpers;
    exports org.apache.commons.logging;
    exports com.github.kjetilv.uplift.flogs;

    provides java.lang.System.LoggerFinder with com.github.kjetilv.uplift.flogs.FloggerFinder;
}
