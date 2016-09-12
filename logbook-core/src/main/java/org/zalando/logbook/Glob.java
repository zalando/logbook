package org.zalando.logbook;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

final class Glob {

    private static final Pattern GLOB = Pattern.compile("\\?|(/\\*{2}$)|\\*{2}|(/\\*$)|\\*");

    private static final Map<String, String> REPLACEMENTS;

    static {
        final Map<String, String> replacements = new HashMap<>();

        replacements.put("?", ".");
        replacements.put("/**", "(/.*)?$");
        replacements.put("**", ".*?");
        replacements.put("/*", "/[^/]*$");

        REPLACEMENTS = Collections.unmodifiableMap(replacements);
    }

    public static Predicate<String> compile(final String glob) {
        return PatternLike.compile(GLOB, glob, Glob::translate);
    }

    private static String translate(final String match) {
        return REPLACEMENTS.getOrDefault(match, "[^/]*?");
    }


}
