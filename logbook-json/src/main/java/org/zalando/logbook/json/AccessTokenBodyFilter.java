package org.zalando.logbook.json;

import org.apiguardian.api.API;
import org.zalando.logbook.BodyFilter;

import javax.annotation.Nullable;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.zalando.logbook.json.JsonBodyFilters.accessToken;

@API(status = INTERNAL)
public final class AccessTokenBodyFilter implements BodyFilter {

    private final BodyFilter delegate = accessToken();

    @Override
    public String filter(@Nullable final String contentType, final String body) {
        return delegate.filter(contentType, body);
    }

}
