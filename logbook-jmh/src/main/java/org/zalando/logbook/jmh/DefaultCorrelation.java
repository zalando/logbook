package org.zalando.logbook.jmh;

import java.time.Duration;
import java.time.Instant;

import org.zalando.logbook.Correlation;

public class DefaultCorrelation implements Correlation {

    private String id;
    private Instant start;
    private Instant end;
    private Duration duration;
    
    public DefaultCorrelation(String id, Instant start, Instant end, Duration duration) {
        super();
        this.id = id;
        this.start = start;
        this.end = end;
        this.duration = duration;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Instant getStart() {
        return start;
    }

    @Override
    public Instant getEnd() {
        return end;
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

}
