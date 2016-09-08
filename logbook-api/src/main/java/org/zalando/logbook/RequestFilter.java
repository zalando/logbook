package org.zalando.logbook;

@FunctionalInterface
public interface RequestFilter {

    HttpRequest filter(final HttpRequest request);

    static RequestFilter none() {
        return request -> request;
    }

    static RequestFilter merge(final RequestFilter left, final RequestFilter right) {
        return request ->
                left.filter(right.filter(request));
    }

}
