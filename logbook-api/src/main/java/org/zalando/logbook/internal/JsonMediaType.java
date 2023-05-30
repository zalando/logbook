package org.zalando.logbook.internal;

import java.util.function.Predicate;

public final class JsonMediaType {

    private JsonMediaType() {
    }

    public static final Predicate<String> JSON = contentType -> {
        if (contentType == null) {
            return false;
        }
        // implementation note: manually coded for improved performance
        if (contentType.startsWith("application/")) {
            int index = contentType.indexOf(';', 12);
            if (index != -1) {
                if (index > 16) {
                    // application/some+json;charset=utf-8
                    return contentType.regionMatches(index - 5, "+json", 0, 5);
                }

                // application/json;charset=utf-8
                return contentType.regionMatches(index - 4, "json", 0, 4);
            } else {
                // application/json
                if (contentType.length() == 16) {
                    return contentType.endsWith("json");
                }
                // application/some+json
                return contentType.endsWith("+json");
            }
        }
        return false;
    };
}
