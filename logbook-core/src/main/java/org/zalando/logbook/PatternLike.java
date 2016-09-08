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
        final String pattern = toPattern(parser, code, translator);

        if (pattern.equals(code)) {
            return code::equals;
        } else {
            final Pattern compile = Pattern.compile(pattern);
            return path -> compile.matcher(path).matches();
        }
    }

    static String toPattern(final Pattern parser, final String code, final String replacement) {
        return toPattern(parser, code, match -> replacement);
    }

    static String toPattern(final Pattern parser, final String code, final UnaryOperator<String> translator) {
        final Matcher matcher = parser.matcher(code);
        final StringBuilder result = new StringBuilder();
        int end = 0;

        while (matcher.find()) {
            result.append(quote(code, end, matcher.start()));

            final String match = matcher.group();
            result.append(translator.apply(match));

            end = matcher.end();
        };

        result.append(quote(code, end, code.length()));
        return result.toString();
    }

    private static String quote(final String s, final int start, final int end) {
        if (start == end) {
            return "";
        }
        return Pattern.quote(s.substring(start, end));
    }

}
