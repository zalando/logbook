package org.zalando.logbook;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

final class JsonHeuristic {

    private final Pattern number = compile("^-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?$");

    boolean isProbablyJson(final String body) {
        return isNull(body)
                || isBoolean(body)
                || isProbablyString(body)
                || isNumber(body)
                || isProbablyArray(body)
                || isProbablyObject(body);
    }

    private boolean isNull(final String body) {
        return "null".equals(body);
    }

    private boolean isBoolean(final String body) {
        return "true".equals(body) || "false".equals(body);
    }

    private boolean isProbablyString(final String body) {
        return body.startsWith("\"") && body.endsWith("\"") && body.length() > 1;
    }

    private boolean isNumber(final String body) {
        return number.matcher(body).matches();
    }

    private boolean isProbablyArray(final String body) {
        return body.startsWith("[") && body.endsWith("]");
    }

    private boolean isProbablyObject(final String body) {
        return body.startsWith("{") && body.endsWith("}");
    }

}
