package org.zalando.logbook;

import java.util.List;
import java.util.Map;

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
        final BaseHttpMessage.HeadersBuilder builder = new BaseHttpMessage.HeadersBuilder();
        for (int i = 0; i < x.length; i += 2) {
            builder.put(x[i], x[i + 1]);
        }
        return builder.build();
    }
}
