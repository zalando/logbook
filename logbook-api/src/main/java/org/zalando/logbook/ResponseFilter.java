package org.zalando.logbook;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
@FunctionalInterface
public interface ResponseFilter {

    /**
     * Marker interface to signal that a filter should be active by default.
     *
     * @see java.util.ServiceLoader
     */
    interface Default extends ResponseFilter {

    }

    HttpResponse filter(final HttpResponse response);

    static ResponseFilter none() {
        return response -> response;
    }

    static ResponseFilter merge(final ResponseFilter left, final ResponseFilter right) {
        return response ->
                left.filter(right.filter(response));
    }

}
