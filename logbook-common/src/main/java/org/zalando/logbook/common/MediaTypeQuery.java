package org.zalando.logbook.common;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class MediaTypeQuery {

    private static final Pattern WILDCARD = Pattern.compile("\\*");

    private MediaTypeQuery() {

    }

    public static Predicate<String> compile(final String query, final String... queries) {
        return Arrays.stream(queries)
                .map(MediaTypeQuery::compile)
                .reduce(compile(query), Predicate::or);
    }

    private static Predicate<String> compile(final String query) {
        final int slash = query.indexOf('/');
        final int semicolon = query.indexOf(';');
        final int end = semicolon == -1 ? query.length() : semicolon;

        final String type = query.substring(0, slash).trim();
        final String subType = query.substring(slash + 1, end).trim();

        final String first = PatternLike.toPattern(WILDCARD, type, ".*?");
        final String second = PatternLike.toPattern(WILDCARD, subType, ".*?");

        // TODO support real matching on parameters
        final Pattern pattern = Pattern.compile(first + '/' + second + "(;.*)?");

        return input -> input != null && pattern.matcher(input).matches();
    }


}
