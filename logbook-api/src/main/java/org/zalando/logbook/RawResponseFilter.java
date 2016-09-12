package org.zalando.logbook;

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
