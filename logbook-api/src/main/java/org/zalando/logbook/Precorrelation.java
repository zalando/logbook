package org.zalando.logbook;

public interface Precorrelation<Request> {

    String getId();

    Request getRequest();

}
