package org.zalando.logbook.api;

import org.apiguardian.api.API;

import java.time.Instant;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface Precorrelation {

    String getId();

    Instant getStart();

    Correlation correlate();

}
