package org.zalando.logbook.jmh;

import java.time.Duration;

import org.zalando.logbook.Correlation;

public class DefaultCorrelation implements Correlation {

    private String id;
    private Duration duration;
    
    public DefaultCorrelation(String id, Duration duration) {
        super();
        this.id = id;
        this.duration = duration;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

}
