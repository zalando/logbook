package org.zalando.logbook;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
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
