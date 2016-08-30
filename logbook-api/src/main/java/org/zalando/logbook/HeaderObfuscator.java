package org.zalando.logbook;

@FunctionalInterface
public interface HeaderObfuscator {

    String obfuscate(final String key, final String value);

    static HeaderObfuscator none() {
        return (key, value) -> value;
    }

    static HeaderObfuscator merge(final HeaderObfuscator left, final HeaderObfuscator right) {
        return (key, value) -> left.obfuscate(key, right.obfuscate(key, value));
    }

}
