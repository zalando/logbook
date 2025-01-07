package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.test.MockHttpRequest;

import java.io.IOException;

import static org.mockito.Mockito.verify;

@LogbookTest
class WriteCustomTest {

    @Autowired
    private Logbook logbook;

    @MockitoBean
    private HttpLogWriter writer;

    @Test
    void shouldUseCustomWriter() throws IOException {
        final HttpRequest request = MockHttpRequest.create();

        logbook.process(request).write();

        verify(writer).isActive();
    }

}
