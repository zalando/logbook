package org.zalando.logbook.core;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.test.MockHttpRequest;
import org.zalando.logbook.test.MockHttpResponse;

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
