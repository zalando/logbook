package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class HttpHeadersTest {

    @Test
    void emptyReturnsSingleton() {
        assertSame(HttpHeaders.empty(), HttpHeaders.empty());
    }

    @Test
    void createsFromExistingMap() {
        final Map<String, List<String>> map = singletonMap(
                "Content-Type", singletonList("application/json"));

        final HttpHeaders unit = HttpHeaders.of(map);

        assertEquals(map, unit);
    }

    @Test
    void uselessOperationsPreserveOriginalInstance() {
        final HttpHeaders unit = HttpHeaders.empty()
                .update("Content-Type", "application/json")
                .update("Host", "localhost");

        final HttpHeaders actual = unit
                .apply("Cookie", previous -> null)
                .apply((name, previous) -> previous)
                .delete("Set-Cookie");

        assertSame(actual, unit);
    }

}
