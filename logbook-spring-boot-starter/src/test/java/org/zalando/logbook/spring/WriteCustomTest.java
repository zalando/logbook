package org.zalando.logbook.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.MockRawHttpRequest;
import org.zalando.logbook.RawHttpRequest;

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
        final RawHttpRequest request = MockRawHttpRequest.create();

        logbook.write(request);

        verify(writer).isActive(request);
    }

}
