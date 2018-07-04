package org.zalando.logbook;

import org.apiguardian.api.API;

import java.time.Duration;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface Correlation<I, O> extends Precorrelation<I> {

    String getId();

    Duration getDuration();

    O getResponse();

    HttpResponse getOriginalResponse();

}
