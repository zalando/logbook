package org.zalando.logbook.common;

import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class Glob {

    private static final Pattern GLOB = Pattern.compile("\\?|(/\\*{2}$)|\\*{2}|(/\\*$)|\\*");

    private static final Map<String, String> REPLACEMENTS = Map.of(
        "?", ".",
        "/**", "(/.*)?$",
        "**", ".*?",
        "/*", "/[^/]*$"
    );

    private Glob() {

    }

    public static Predicate<String> compile(final String glob) {
        return PatternLike.compile(GLOB, glob, Glob::translate);
    }

    private static String translate(final String match) {
        return REPLACEMENTS.getOrDefault(match, "[^/]*?");
    }


}
