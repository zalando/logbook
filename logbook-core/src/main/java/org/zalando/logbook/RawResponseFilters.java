package org.zalando.logbook;

import org.apiguardian.api.API;

import javax.annotation.Nullable;

import static org.apiguardian.api.API.Status.MAINTAINED;
import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public final class RawResponseFilters {

    private RawResponseFilters() {

    }

    @API(status = MAINTAINED)
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
