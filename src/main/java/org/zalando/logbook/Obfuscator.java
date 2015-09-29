package org.zalando.logbook;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

// TODO implement
// TODO find a nice way for body obfuscation
@FunctionalInterface
public interface Obfuscator {

    String obfuscate(final String key, final String value);

    static Obfuscator none() {
        return (key, value) -> value;
    }

    static Obfuscator obfuscate(final Predicate<String> keyPredicate, final String replacement) {
        return (key, value) -> keyPredicate.test(key) ? replacement : value;
    }

    static Obfuscator obfuscate(final BiPredicate<String, String> predicate, final String replacement) {
        return (key, value) -> predicate.test(key, value) ? replacement : value;
    }

    static Obfuscator compound(final Obfuscator... obfuscators) {
        return (key, value) -> {
            for (Obfuscator obfuscator : obfuscators) {
                final String replacement = obfuscator.obfuscate(key, value);
                if (!Objects.equals(replacement, value)) {
                    return replacement;
                }
            }

            return value;
        };
    }

    static Obfuscator authorization() {
        return obfuscate("Authorization"::equalsIgnoreCase, "XXX");
    }

}
