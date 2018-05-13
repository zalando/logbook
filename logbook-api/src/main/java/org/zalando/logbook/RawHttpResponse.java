package org.zalando.logbook;

import org.apiguardian.api.API;

import java.io.IOException;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface RawHttpResponse extends BaseHttpResponse {

    HttpResponse withBody() throws IOException;

    default void withoutBody() throws IOException {
        // omitting the body is the default behavior, unless withBody has been called
    }

}
