module uplift.flogs {
    requires java.logging;
    requires java.desktop;

    exports org.slf4j;
    exports org.slf4j.event;
    exports org.slf4j.helpers;
    exports org.apache.commons.logging;
    exports com.github.kjetilv.uplift.flogs;
}
