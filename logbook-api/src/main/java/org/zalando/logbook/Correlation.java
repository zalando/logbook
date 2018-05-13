package org.zalando.logbook;

import org.apiguardian.api.API;

import java.time.Duration;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface Correlation<I, O> {

    String getId();

    Duration getDuration();

    I getRequest();

    O getResponse();

    HttpRequest getOriginalRequest();

    HttpResponse getOriginalResponse();

}
