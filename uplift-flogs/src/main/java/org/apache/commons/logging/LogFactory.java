package org.apache.commons.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "unused", "WeakerAccess" })
public final class LogFactory {

    public static Log getLog(Class<?> clazz) {
        return getLog(clazz.getName());
    }

    public static Log getLog(String name) {
        Logger l = LoggerFactory.getLogger(name);
        return new Log() {

            @Override
            public void debug(Object message) {
                l.debug(String.valueOf(message));
            }

            @Override
            public void debug(Object message, Throwable t) {
                l.debug(String.valueOf(message), t);
            }

            @Override
            public void error(Object message) {
                l.error(String.valueOf(message));
            }

            @Override
            public void error(Object message, Throwable t) {
                l.error(String.valueOf(message), t);
            }

            @Override
            public void fatal(Object message) {
                l.error(String.valueOf(message));
            }

            @Override
            public void fatal(Object message, Throwable t) {
                l.error(String.valueOf(message), t);
            }

            @Override
            public void info(Object message) {
                l.info(String.valueOf(message));
            }

            @Override
            public void info(Object message, Throwable t) {
                l.info(String.valueOf(message), t);
            }

            @Override
            public boolean isDebugEnabled() {
                return l.isDebugEnabled();
            }

            @Override
            public boolean isErrorEnabled() {
                return l.isErrorEnabled();
            }

            @Override
            public boolean isFatalEnabled() {
                return l.isErrorEnabled();
            }

            @Override
            public boolean isInfoEnabled() {
                return l.isInfoEnabled();
            }

            @Override
            public boolean isTraceEnabled() {
                return l.isTraceEnabled();
            }

            @Override
            public boolean isWarnEnabled() {
                return l.isWarnEnabled();
            }

            @Override
            public void trace(Object message) {
                l.trace(String.valueOf(message));
            }

            @Override
            public void trace(Object message, Throwable t) {
                l.trace(String.valueOf(message), t);
            }

            @Override
            public void warn(Object message) {
                l.warn(String.valueOf(message));
            }

            @Override
            public void warn(Object message, Throwable t) {
                l.warn(String.valueOf(message), t);
            }
        };
    }

    private LogFactory() {
    }
}
