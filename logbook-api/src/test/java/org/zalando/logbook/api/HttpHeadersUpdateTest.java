package org.zalando.logbook.api;

import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

class HttpHeadersUpdateTest {

    @Test
    void updatesMap() {
        final HttpHeaders unit = HttpHeaders.empty()
                .update("Content-Type", "application/json")
                .update(singletonMap("Cookie", list("user=me")));

        assertThat(unit)
                .containsEntry("Content-Type", list("application/json"))
                .containsEntry("Cookie", list("user=me"));
    }

    private static <T> List<T> list(final T s) {
        return singletonList(s);
    }

}
