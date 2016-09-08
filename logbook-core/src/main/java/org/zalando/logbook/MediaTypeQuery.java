package org.zalando.logbook;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static org.zalando.logbook.PatternLike.toPattern;

final class MediaTypeQuery {

    private static final Pattern WILDCARD = Pattern.compile("\\*");

    MediaTypeQuery() {
        // package private so we can trick code coverage
    }

    public static Predicate<String> compile(final String... queries) {
        return Arrays.stream(queries)
                .map(MediaTypeQuery::compile)
                .reduce(Predicate::or)
                .orElse($ -> false);
    }

    public static Predicate<String> compile(final String query) {
        final int slash = query.indexOf('/');
        final int semicolon = query.indexOf(';');
        final int end = semicolon == -1 ? query.length() : semicolon;

        final String type = query.substring(0, slash).trim();
        final String subType = query.substring(slash + 1, end).trim();

        final String first = toPattern(WILDCARD, type, ".*?");
        final String second = toPattern(WILDCARD, subType, ".*?");

        // TODO support real matching on parameters
        final Pattern pattern = Pattern.compile(first + '/' + second + "(;.*)?");

        return input -> input != null && pattern.matcher(input).matches();
    }


}
