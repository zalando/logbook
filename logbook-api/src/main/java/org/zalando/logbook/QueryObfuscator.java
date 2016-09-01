package org.zalando.logbook;

@FunctionalInterface
public interface QueryObfuscator {

    String obfuscate(final String query);

    static QueryObfuscator none() {
        return query -> query;
    }

    static QueryObfuscator merge(final QueryObfuscator left, final QueryObfuscator right) {
        return query -> left.obfuscate(right.obfuscate(query));
    }

}
