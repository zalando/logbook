package org.zalando.logbook;

import java.time.Duration;

public interface Correlation<Request, Response> {

    String getId();

    Duration getDuration();

    Request getRequest();

    Response getResponse();

    HttpRequest getOriginalRequest();

    HttpResponse getOriginalResponse();

}
