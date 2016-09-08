package org.zalando.logbook;

@FunctionalInterface
public interface ResponseFilter {

    HttpResponse filter(final HttpResponse response);

    static ResponseFilter none() {
        return response -> response;
    }

    static ResponseFilter merge(final ResponseFilter left, final ResponseFilter right) {
        return response ->
                left.filter(right.filter(response));
    }

}
