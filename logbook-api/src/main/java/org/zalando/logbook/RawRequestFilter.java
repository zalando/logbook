package org.zalando.logbook;

@FunctionalInterface
public interface RawRequestFilter {

    RawHttpRequest filter(final RawHttpRequest request);

    static RawRequestFilter none() {
        return request -> request;
    }

    static RawRequestFilter merge(final RawRequestFilter left, final RawRequestFilter right) {
        return request ->
                left.filter(right.filter(request));
    }

}
