package org.zalando.logbook;

@FunctionalInterface
public interface QueryFilter {

    String filter(final String query);

    static QueryFilter none() {
        return query -> query;
    }

    static QueryFilter merge(final QueryFilter left, final QueryFilter right) {
        return query ->
                left.filter(right.filter(query));
    }

}
