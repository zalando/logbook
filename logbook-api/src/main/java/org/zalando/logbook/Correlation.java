package org.zalando.logbook;

public interface Correlation<Request, Response> {

    String getId();

    Request getRequest();

    Response getResponse();

}
