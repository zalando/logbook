package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

class HttpHeadersUpdateTest {

    @Test
    void updatesMap() {
        final HttpHeaders unit = HttpHeaders.empty()
                .update("Content-Type", "application/json")
                .update(singletonMap("Cookie", list("user=me")));

        assertThat(unit, hasEntry("Content-Type", list("application/json")));
        assertThat(unit, hasEntry("Cookie", list("user=me")));
    }

    private static <T> List<T> list(final T s) {
        return singletonList(s);
    }

}
