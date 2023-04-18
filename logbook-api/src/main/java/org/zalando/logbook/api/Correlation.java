package org.zalando.logbook.api;

import org.apiguardian.api.API;

import java.time.Duration;
import java.time.Instant;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface Correlation extends Precorrelation {

    Instant getEnd();

    Duration getDuration();

    @Override
    default Correlation correlate() {
        return this;
    }
}
