package org.zalando.logbook.benchmark.jmh;

import org.zalando.logbook.Correlation;
import org.zalando.logbook.Precorrelation;

import java.time.Instant;

public class DefaultPrecorrelation implements Precorrelation {

    private String id;
    private Correlation correlation;

    public DefaultPrecorrelation(final String id, final Correlation correlation) {
        super();
        this.id = id;
        this.correlation = correlation;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Instant getStart() {
        return correlation.getStart();
    }

    @Override
    public Correlation correlate() {
        return correlation;
    }

}
