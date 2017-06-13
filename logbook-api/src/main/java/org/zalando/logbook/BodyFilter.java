package org.zalando.logbook;

import javax.annotation.Nullable;

@FunctionalInterface
public interface BodyFilter {

    String filter(@Nullable final String contentType, final String body);
    
    static BodyFilter none() {
        return (contentType, body) -> body;
    }

    static BodyFilter merge(final BodyFilter left, final BodyFilter right) {
        return (contentType, body) ->
                left.filter(contentType, right.filter(contentType, body));
    }

}
