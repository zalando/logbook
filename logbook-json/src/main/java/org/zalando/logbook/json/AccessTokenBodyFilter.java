package org.zalando.logbook.json;

import lombok.experimental.Delegate;
import org.apiguardian.api.API;
import org.zalando.logbook.api.BodyFilter;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.zalando.logbook.json.JsonBodyFilters.accessToken;

@API(status = INTERNAL)
public final class AccessTokenBodyFilter implements BodyFilter {

    @Delegate
    private final BodyFilter delegate = accessToken();

}
