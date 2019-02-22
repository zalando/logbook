package org.zalando.logbook;

import org.apiguardian.api.API;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apiguardian.api.API.Status.MAINTAINED;

@API(status = MAINTAINED)
public final class MockHeaders {

    MockHeaders() {
        // package private so we can trick code coverage
    }

    public static Map<String, List<String>> of(final String k1, final String v1) {
        return buildHeaders(k1, v1);
    }

    public static Map<String, List<String>> of(final String k1, final String v1, final String k2, final String v2) {
        return buildHeaders(k1, v1, k2, v2);
    }

    public static Map<String, List<String>> of(final String k1, final String v1, final String k2, final String v2,
            final String k3, final String v3) {
        return buildHeaders(k1, v1, k2, v2, k3, v3);
    }

    private static Map<String, List<String>> buildHeaders(final String... x) {
        final Map<String, List<String>> headers = Headers.empty();

        for (int i = 0; i < x.length; i += 2) {
            final String value = x[i + 1];
            headers.compute(x[i], (key, before) -> {
                final List<String> after = Optional.ofNullable(before).orElseGet(ArrayList::new);
                after.add(value);
                return after;
            });
        }

        return headers;
    }
}
