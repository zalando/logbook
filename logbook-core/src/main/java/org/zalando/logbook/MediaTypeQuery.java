package org.zalando.logbook;

import java.util.function.Predicate;
import java.util.regex.Pattern;

final class MediaTypeQuery {

    private static final Pattern WILDCARD = Pattern.compile("\\*");

    MediaTypeQuery() {
        // package private so we can trick code coverage
    }

    public static Predicate<String> compile(final String query) {
        return PatternLike.compile(WILDCARD, query, match -> ".*?");
    }

}
