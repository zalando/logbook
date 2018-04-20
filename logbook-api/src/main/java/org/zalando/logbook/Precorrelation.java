package org.zalando.logbook;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface Precorrelation<Request> {

    String getId();

    Request getRequest();

    HttpRequest getOriginalRequest();

}
