package org.zalando.logbook;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Glob {

    private static final Pattern GLOB = Pattern.compile("\\?|(/\\*{2}$)|\\*{2}|(/\\*$)|\\*");

    public static Predicate<String> compile(final String glob) {
        final StringBuilder result = new StringBuilder();
        final Matcher matcher = GLOB.matcher(glob);

        int end = 0;

        if (matcher.find()) {
            do {
                result.append(quote(glob, end, matcher.start()));

                final String match = matcher.group();
                result.append(translate(match));

                end = matcher.end();
            } while (matcher.find());
        } else {
            return glob::equals;
        }

        result.append(quote(glob, end, glob.length()));
        final Pattern pattern = Pattern.compile(result.toString());
        return path -> pattern.matcher(path).matches();
    }

    // https://github.com/jacoco/jacoco/wiki/FilteringOptions
    @SuppressWarnings("IfCanBeSwitch") // jacoco can't handle string-switch correctly
    private static String translate(final String match) {
        if (match.equals("?")) {
            return ".";
        } else if (match.equals("/**")) {
            return "(/.*)?$";
        } else if (match.equals("**")) {
            return ".*?";
        } else if (match.equals("/*")) {
            return "/[^/]*$";
        } else {
            return "[^/]*?";
        }
    }

    private static String quote(final String s, final int start, final int end) {
        if (start == end) {
            return "";
        }
        return Pattern.quote(s.substring(start, end));
    }

}
