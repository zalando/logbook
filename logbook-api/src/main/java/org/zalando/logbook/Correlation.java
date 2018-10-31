package org.zalando.logbook;

import org.apiguardian.api.API;

import java.time.Duration;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface Correlation extends Precorrelation {

    String getId();

    Duration getDuration();

    @Override
    default Correlation correlate() {
        return this;
    }

}
