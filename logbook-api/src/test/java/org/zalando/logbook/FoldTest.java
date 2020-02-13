package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.Fold.NoCombiner;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FoldTest {

    @Test
    void combinerIsNotSupported() {
        final NoCombiner unit = NoCombiner.NONE;

        assertThrows(UnsupportedOperationException.class, () ->
                unit.apply(null, null));
    }

}
