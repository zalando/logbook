package org.zalando.logbook;

import org.apiguardian.api.API;

import javax.annotation.Nullable;

import static org.apiguardian.api.API.Status.MAINTAINED;
import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public final class RequestFilters {

    private RequestFilters() {

    }

    @API(status = MAINTAINED)
    public static RequestFilter defaultValue() {
        return replaceBody(BodyReplacers.defaultValue());
    }

    public static RequestFilter replaceBody(final BodyReplacer<HttpRequest> replacer) {
        return request -> {
            @Nullable final String replacement = replacer.replace(request);
            return replacement == null ?
                    request :
                    new BodyReplacementHttpRequest(request, replacement);
        };
    }

}
