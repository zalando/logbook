package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.zalando.logbook.HttpHeaders.predicate;

class HttpHeadersApplyTest {

    private final HttpHeaders unit = HttpHeaders.empty()
            .update("Content-Type", "application/json")
            .update(singletonMap("Cookie", list("user=me")))
            .update("Host", "localhost");

    @Test
    void appliesToSingleHeader() {
        final HttpHeaders actual = unit
                .apply("Content-Type", previous -> null)
                .apply("Cookie", previous -> list("user=you"));

        assertThat(actual, not(hasKey("Content-Type")));
        assertThat(actual, hasEntry("Cookie", list("user=you")));
        assertThat(actual, hasEntry("Host", list("localhost")));
    }
    @Test
    void appliesToMultipleHeaders() {
        final HttpHeaders actual = unit
                .apply(
                        Arrays.asList("Content-Type", "Cookie"),
                        (name, previous) -> emptyList());

        assertThat(actual, hasEntry("Content-Type", emptyList()));
        assertThat(actual, hasEntry("Cookie", emptyList()));
        assertThat(actual, hasEntry("Host", list("localhost")));
    }

    @Test
    void appliesToMatchingHeaders() {
        final HttpHeaders actual = unit
                .apply(
                        predicate(s -> s.contains("e")),
                        (name, previous) -> emptyList())
                .apply(
                        predicate("Host"::equals),
                        (name, previous) -> null);

        assertThat(actual, hasEntry("Content-Type", emptyList()));
        assertThat(actual, hasEntry("Cookie", emptyList()));
        assertThat(actual, not(hasKey("Host")));
    }

    @Test
    void appliesToEach() {
        final HttpHeaders actual = unit
                .apply((name, previous) -> singletonList("Not " + name));

        assertThat(actual, hasEntry("Content-Type", list("Not Content-Type")));
        assertThat(actual, hasEntry("Cookie", list("Not Cookie")));
        assertThat(actual, hasEntry("Host", list("Not Host")));
    }

    @Test
    void deletesMatchingHeaders() {
        final HttpHeaders actual = unit
                .delete(predicate(s -> s.startsWith("C")));

        assertThat(actual, not(hasKey("Content-Type")));
        assertThat(actual, not(hasKey("Cookie")));
        assertThat(actual, hasEntry("Host", list("localhost")));
    }

    private static <T> List<T> list(final T s) {
        return singletonList(s);
    }

}
