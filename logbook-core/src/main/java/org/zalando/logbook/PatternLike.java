package org.zalando.logbook;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class PatternLike {

    PatternLike() {
        // package private so we can trick code coverage
    }

    static Predicate<String> compile(final Pattern parser, final String code, final UnaryOperator<String> translator) {
        final StringBuilder result = new StringBuilder();
        final Matcher matcher = parser.matcher(code);

        int end = 0;

        if (matcher.find()) {
            do {
                result.append(quote(code, end, matcher.start()));

                final String match = matcher.group();
                result.append(translator.apply(match));

                end = matcher.end();
            } while (matcher.find());
        } else {
            return code::equals;
        }

        result.append(quote(code, end, code.length()));
        final Pattern pattern = Pattern.compile(result.toString());
        return path -> pattern.matcher(path).matches();
    }

    private static String quote(final String s, final int start, final int end) {
        if (start == end) {
            return "";
        }
        return Pattern.quote(s.substring(start, end));
    }

}
