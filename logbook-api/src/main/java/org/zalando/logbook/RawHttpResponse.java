package org.zalando.logbook;

import java.io.IOException;

public interface RawHttpResponse extends BaseHttpResponse {

    HttpResponse withBody() throws IOException;

    default void withoutBody() throws IOException {

    }

}
