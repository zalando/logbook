package org.zalando.logbook;

import org.apiguardian.api.API;

import java.io.IOException;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface HttpResponse extends HttpMessage {

    int getStatus();

    // TODO void vs pseudo-function (mutable)
    HttpResponse withBody() throws IOException;

    HttpResponse withoutBody();

}
