package org.zalando.logbook.servlet;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.DefaultHttpLogWriter;
import org.zalando.logbook.Logbook;

import static org.slf4j.LoggerFactory.getLogger;
import static org.zalando.logbook.DefaultHttpLogWriter.Level.INFO;

class LogbookFilterTest {

    @Test
    void shouldBeNonFinalToAllowExtending() {
        new CustomLogbookFilter(); // check this compiles
    }

    private static class CustomLogbookFilter extends LogbookFilter {

        public CustomLogbookFilter() {
            super(customLogbook(), Strategy.NORMAL);
        }

        private static Logbook customLogbook() {
            return Logbook.builder().writer(new DefaultHttpLogWriter(getLogger(LogbookFilter.class), INFO)).build();
        }
    }

}