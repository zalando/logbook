package org.zalando.logbook;

import java.io.IOException;

public interface RawHttpRequest extends BaseHttpRequest {

    HttpRequest withBody() throws IOException;

    default void withoutBody() throws IOException {

    }

}
