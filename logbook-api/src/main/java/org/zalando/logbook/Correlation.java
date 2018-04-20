package org.zalando.logbook;

import org.apiguardian.api.API;

import java.time.Duration;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface Correlation<Request, Response> {

    String getId();

    Duration getDuration();

    Request getRequest();

    Response getResponse();

    HttpRequest getOriginalRequest();

    HttpResponse getOriginalResponse();

}
