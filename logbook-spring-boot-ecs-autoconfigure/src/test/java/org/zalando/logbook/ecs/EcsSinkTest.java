package org.zalando.logbook.ecs;

import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Sink;

import static ch.qos.logback.classic.Level.INFO;
import static ch.qos.logback.classic.Level.TRACE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EcsSinkTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(Logbook.class);

    @AfterEach
    void reset() {
        logger.setLevel(null);
    }

    @Test
    void shouldUseTraceLevelForActivation() {
        final Sink unit = new EcsSink(content -> "");

        logger.setLevel(TRACE);
        assertTrue(unit.isActive());

        logger.setLevel(INFO);
        assertFalse(unit.isActive());
    }

}
