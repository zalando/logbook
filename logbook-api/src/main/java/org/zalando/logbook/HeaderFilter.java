package org.zalando.logbook;

@FunctionalInterface
public interface HeaderFilter {

    String filter(final String key, final String value);

    static HeaderFilter none() {
        return (key, value) -> value;
    }

    static HeaderFilter merge(final HeaderFilter left, final HeaderFilter right) {
        return (key, value) -> left.filter(key, right.filter(key, value));
    }

}
