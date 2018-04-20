package org.zalando.logbook;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
@FunctionalInterface
public interface RawResponseFilter {

    RawHttpResponse filter(final RawHttpResponse response);

    static RawResponseFilter none() {
        return response -> response;
    }

    static RawResponseFilter merge(final RawResponseFilter left, final RawResponseFilter right) {
        return response ->
                left.filter(right.filter(response));
    }

}
