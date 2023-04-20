package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(actual)
                .doesNotContainKey("Content-Type")
                .containsEntry("Cookie", list("user=you"))
                .containsEntry("Host", list("localhost"));
    }

    @Test
    void appliesToMultipleHeaders() {
        final HttpHeaders actual = unit
                .apply(
                        Arrays.asList("Content-Type", "Cookie"),
                        (name, previous) -> emptyList());

        assertThat(actual)
                .containsEntry("Content-Type", emptyList())
                .containsEntry("Cookie", emptyList())
                .containsEntry("Host", list("localhost"));
    }

    @Test
    void appliesToMatchingHeaders() {
        final HttpHeaders actual = unit
                .apply(
                        HttpHeaders.predicate(s -> s.contains("e")),
                        (name, previous) -> emptyList())
                .apply(
                        HttpHeaders.predicate("Host"::equals),
                        (name, previous) -> null);

        assertThat(actual)
                .containsEntry("Content-Type", emptyList())
                .containsEntry("Cookie", emptyList())
                .doesNotContainKey("Host");
    }

    @Test
    void appliesToEach() {
        final HttpHeaders actual = unit
                .apply((name, previous) -> singletonList("Not " + name));

        assertThat(actual)
                .containsEntry("Content-Type", list("Not Content-Type"))
                .containsEntry("Cookie", list("Not Cookie"))
                .containsEntry("Host", list("Not Host"));
    }

    @Test
    void deletesMatchingHeaders() {
        final HttpHeaders actual = unit
                .delete(HttpHeaders.predicate(s -> s.startsWith("C")));

        assertThat(actual)
                .doesNotContainKeys("Content-Type", "Cookie")
                .containsEntry("Host", list("localhost"));
    }

    private static <T> List<T> list(final T s) {
        return singletonList(s);
    }

}
