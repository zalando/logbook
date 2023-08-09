package org.zalando.logbook.core;

import lombok.experimental.Delegate;
import org.apiguardian.api.API;
import org.zalando.logbook.BodyFilter;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.zalando.logbook.core.BodyFilters.truncate;

@API(status = INTERNAL)
public final class TruncatingBodyFilter implements BodyFilter {

    @Delegate
    private final BodyFilter delegate;

    public TruncatingBodyFilter(final int maxBodySize) {
        if (maxBodySize < 0) {
            this.delegate = (contentType, body) -> body;
        } else {
            this.delegate = truncate(maxBodySize);
        }
    }
}
