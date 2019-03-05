package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class DefaultLogbookFactoryTest {

    private final Logbook logbook = Logbook.create();
    private final HttpRequest request = MockHttpRequest.create();
    private final HttpResponse response = MockHttpResponse.create();

    @Test
    void shouldCreateWithDefaults() throws IOException {
        logbook.process(request).write().process(response).write();
    }

}
