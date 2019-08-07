package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.time.Clock;

import static java.time.Duration.between;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SimplePrecorrelationTest {

    private final Precorrelation unit = new SimplePrecorrelation(
            Clock.systemUTC());

    @Test
    void getId() {
        assertNotNull(unit.getId());
    }

    @Test
    void getStart() {
        assertNotNull(unit.getStart());
    }

    @Test
    void correlate() {
        final Correlation correlation = unit.correlate();

        assertEquals(unit.getId(), correlation.getId());
        assertEquals(unit.getStart(), correlation.getStart());
        assertNotNull(correlation.getEnd());
        assertEquals(between(correlation.getStart(), correlation.getEnd()), correlation.getDuration());
    }

}
