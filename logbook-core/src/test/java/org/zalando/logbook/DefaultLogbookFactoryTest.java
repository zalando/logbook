package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;

class DefaultLogbookFactoryTest {

    private final Logbook logbook = Logbook.create();
    private final HttpRequest request = mock(HttpRequest.class);
    private final HttpResponse response = mock(HttpResponse.class);

    @Test
    void shouldCreateWithDefaults() throws IOException {
        logbook.process(request).write().process(response).write();
    }

}
