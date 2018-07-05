package org.zalando.logbook;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

final class JsonHeuristic {

    private final Pattern number = compile("^-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?$");

    boolean isProbablyJson(final String body) {
        // Insignificant whitespace is allowed before or after any of the six structural characters.
        // https://tools.ietf.org/html/rfc4627#section-2
        final String trimmed = body.trim();

        return isProbablyObject(trimmed)
                || isProbablyArray(trimmed)
                || isProbablyString(body)
                || isNumber(body)
                || isBoolean(body)
                || isNull(body);
    }

    private boolean isProbablyObject(final String body) {
        return body.startsWith("{") && body.endsWith("}");
    }

    private boolean isProbablyArray(final String body) {
        return body.startsWith("[") && body.endsWith("]");
    }

    private boolean isProbablyString(final String body) {
        return body.startsWith("\"") && body.endsWith("\"") && body.length() > 1;
    }

    private boolean isNumber(final String body) {
        return number.matcher(body).matches();
    }

    private boolean isBoolean(final String body) {
        return "true".equals(body) || "false".equals(body);
    }

    private boolean isNull(final String body) {
        return "null".equals(body);
    }

}
