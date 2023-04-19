package org.zalando.logbook.api;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.api.Fold.NoCombiner;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FoldTest {

    @Test
    void combinerIsNotSupported() {
        final NoCombiner unit = NoCombiner.NONE;

        assertThrows(UnsupportedOperationException.class, () ->
                unit.apply(null, null));
    }

}
