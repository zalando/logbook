package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.MockHttpRequest;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@LogbookTest(properties = "logbook.format.style = splunk")
class FormatStyleSplunkTest {

    @Autowired
    private Logbook logbook;

    @MockBean
    private HttpLogWriter writer;

    @BeforeEach
    void setUp() {
        doReturn(true).when(writer).isActive();
    }

    @Test
    void shouldUseSplunkFormatter() throws IOException {
        logbook.process(MockHttpRequest.create()).write();

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Precorrelation.class), captor.capture());
        assertThat(captor.getValue()).containsSubsequence(
                "protocol=HTTP/1.1",
                "method=GET",
                "uri=http://localhost/"
        );
    }

}
