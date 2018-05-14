package org.zalando.logbook;

import org.apiguardian.api.API;

import javax.annotation.Nullable;

import static org.apiguardian.api.API.Status.MAINTAINED;
import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public final class RawRequestFilters {

    private RawRequestFilters() {

    }

    @API(status = MAINTAINED)
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
