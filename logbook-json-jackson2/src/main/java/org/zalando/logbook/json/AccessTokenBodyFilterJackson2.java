package org.zalando.logbook.json;

import lombok.experimental.Delegate;
import org.apiguardian.api.API;
import org.zalando.logbook.BodyFilter;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.zalando.logbook.json.JsonBodyFiltersJackson2.accessToken;

@Deprecated(since = "4.0.0", forRemoval = true)
@API(status = INTERNAL)
public final class AccessTokenBodyFilterJackson2 implements BodyFilter {
    @Delegate
    private final BodyFilter delegate = accessToken();
}
