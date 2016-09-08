package org.zalando.logbook;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.quote;

public final class QueryFilters {

    QueryFilters() {
        // package private so we can trick code coverage
    }

    public static QueryFilter defaultValue() {
        return accessToken();
    }

    public static QueryFilter accessToken() {
        return replaceQuery("access_token", "XXX");
    }

    public static QueryFilter replaceQuery(final String name, final String replacement) {
        final Pattern pattern = Pattern.compile("((?:^|&)" + quote(name) + "=)(?:.*?)(&|$)");
        final String replacementPattern = "$1" + replacement + "$2";

        return query -> pattern.matcher(query).replaceAll(replacementPattern);
    }

}
