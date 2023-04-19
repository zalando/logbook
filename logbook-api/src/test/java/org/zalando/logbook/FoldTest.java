package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FoldTest {

    @Test
    void combinerIsNotSupported() {
        final Fold.NoCombiner unit = Fold.NoCombiner.NONE;

        assertThrows(UnsupportedOperationException.class, () ->
                unit.apply(null, null));
    }

}
