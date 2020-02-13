package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("ResultOfMethodCallIgnored")
class HttpHeadersImmutabilityTest {

    @Test
    void isImmutable() {
        final HttpHeaders unit = HttpHeaders.empty();
        unit.update("Content-Type", "application/json");
        assertEquals(HttpHeaders.empty(), unit);
    }

    @Test
    void isDeeplyImmutable() {
        final HttpHeaders unit = HttpHeaders.empty()
                .update("Content-Type", "application/json");

        assertThrows(UnsupportedOperationException.class, () ->
                unit.apply("Content-Type", previous -> {
                    previous.clear();
                    return emptyList();
                }));
    }

}
