package org.zalando.logbook;

import javax.annotation.Nullable;

public final class RawRequestFilters {

    RawRequestFilters() {
        // package private so we can trick code coverage
    }

    public static RawRequestFilter defaultValue() {
        return replaceBody(BodyReplacers.defaultValue());
    }

    public static RawRequestFilter replaceBody(final BodyReplacer<RawHttpRequest> replacer) {
        return request -> {
            @Nullable final String replacement = replacer.replace(request);
            return replacement == null ?
                    request :
                    new BodyReplacementRawHttpRequest(request, replacement);
        };
    }

}
