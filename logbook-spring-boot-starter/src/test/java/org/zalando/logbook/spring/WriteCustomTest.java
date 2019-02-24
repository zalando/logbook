package org.zalando.logbook.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.MockHttpRequest;

import java.io.IOException;

import static org.mockito.Mockito.verify;

@LogbookTest
class WriteCustomTest {

    @Autowired
    private Logbook logbook;

    @MockBean
    private HttpLogWriter writer;

    @Test
    void shouldUseCustomWriter() throws IOException {
        final HttpRequest request = MockHttpRequest.create();

        logbook.process(request).write();

        verify(writer).isActive();
    }

}
