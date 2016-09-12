package org.zalando.logbook;

import javax.annotation.Nullable;

public final class RawResponseFilters {

    RawResponseFilters() {
        // package private so we can trick code coverage
    }

    public static RawResponseFilter defaultValue() {
        return replaceBody(BodyReplacers.defaultValue());
    }

    public static RawResponseFilter replaceBody(final BodyReplacer<RawHttpResponse> replacer) {
        return response -> {
            @Nullable final String replacement = replacer.replace(response);
            return replacement == null ?
                    response :
                    new BodyReplacementRawHttpResponse(response, replacement);
        };
    }

}
