package org.zalando.logbook.benchmark.jmh;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

final public class NOPAppender extends AppenderBase<ILoggingEvent> {

    @Override
    protected void append(final ILoggingEvent eventObject) {
        // make sure the event call cannot be discarded by the JIT compiler
        if (eventObject == null || eventObject.getMessage() == null) {
            throw new IllegalArgumentException();
        }
    }
}
