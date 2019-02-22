package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HeadersTest {

    @Test
    void shouldMakeHeadersUnmodifiable() {
        final Map<String, List<String>> original = Headers.empty();

        original.put("Authorization", new ArrayList<>(Arrays.asList(
                "Basic dXNlcjpzZWNyZXQK",
                "Bearer eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.e30.")));

        original.put("Content-Type", new ArrayList<>(singletonList("text/plain")));

        final Map<String, List<String>> unit = Headers.immutableCopy(original);

        assertThrows(UnsupportedOperationException.class, unit::clear);
        assertThrows(UnsupportedOperationException.class, unit.get("Authorization")::clear);
    }

    @Test
    void shouldNotAllowNewHeaders() {
        final Map<String, List<String>> original = Headers.empty();
        original.put("Authorization", new ArrayList<>(singletonList("Basic dXNlcjpzZWNyZXQK")));

        final Map<String, List<String>> unit = Headers.immutableCopy(original);

        assertThat(unit, aMapWithSize(1));

        original.put("Content-Type", new ArrayList<>(singletonList("text/plain")));

        assertThat(unit, aMapWithSize(1));
    }

    @Test
    void shouldNotAllowNewValues() {
        final Map<String, List<String>> original = Headers.empty();
        original.put("Authorization", new ArrayList<>(singletonList("Basic dXNlcjpzZWNyZXQK")));

        final Map<String, List<String>> unit = Headers.immutableCopy(original);

        assertThat(unit.get("Authorization"), hasSize(1));

        original.get("Authorization").add("Bearer eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.e30.");

        assertThat(unit.get("Authorization"), hasSize(1));
    }

}
