package org.zalando.logbook;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.quote;
import static java.util.stream.Collectors.toList;

public final class Obfuscators {

    Obfuscators() {
        // package private so we can trick code coverage
    }

    public static QueryObfuscator obfuscate(final String name, final String replacement) {
        final Pattern pattern = Pattern.compile("((?:^|&)" + quote(name) + "=)(?:.*?)(&|$)");
        final String replacementPattern = "$1" + replacement + "$2";

        return query -> pattern.matcher(query).replaceAll(replacementPattern);
    }

    public static QueryObfuscator accessToken() {
        return obfuscate("access_token", "XXX");
    }

    public static HeaderObfuscator obfuscate(final Predicate<String> keyPredicate, final String replacement) {
        return (key, value) -> keyPredicate.test(key) ? replacement : value;
    }

    public static HeaderObfuscator obfuscate(final BiPredicate<String, String> predicate, final String replacement) {
        return (key, value) -> predicate.test(key, value) ? replacement : value;
    }

    public static HeaderObfuscator authorization() {
        return obfuscate("Authorization"::equalsIgnoreCase, "XXX");
    }

    static Map<String, List<String>> obfuscateHeaders(final Map<String, List<String>> map, final BiFunction<String, String, String> f) {
        final BaseHttpMessage.HeadersBuilder builder = new BaseHttpMessage.HeadersBuilder();
        for (final Map.Entry<String, List<String>> e : map.entrySet()) {
            final String k = e.getKey();
            builder.put(k, e.getValue().stream().map(x -> f.apply(k, x)).collect(toList()));
        }
        return builder.build();
    }
}
