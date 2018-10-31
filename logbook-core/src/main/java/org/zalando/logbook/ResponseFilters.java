package org.zalando.logbook;

import org.apiguardian.api.API;

import javax.annotation.Nullable;

import static org.apiguardian.api.API.Status.MAINTAINED;
import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public final class ResponseFilters {

    private ResponseFilters() {

    }

    @API(status = MAINTAINED)
    public static ResponseFilter defaultValue() {
        return replaceBody(BodyReplacers.defaultValue());
    }

    public static ResponseFilter replaceBody(final BodyReplacer<HttpResponse> replacer) {
        return response -> {
            @Nullable final String replacement = replacer.replace(response);
            return replacement == null ?
                    response :
                    new BodyReplacementHttpResponse(response, replacement);
        };
    }

}
