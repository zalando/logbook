package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.zalando.logbook.MockHttpRequest;
import org.zalando.logbook.api.HttpLogWriter;
import org.zalando.logbook.api.Logbook;
import org.zalando.logbook.api.Precorrelation;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@LogbookTest
class FormatStyleDefaultTest {

    @Autowired
    private Logbook logbook;

    @MockBean
    private HttpLogWriter writer;

    @BeforeEach
    void setUp() throws IOException {
        doReturn(true).when(writer).isActive();
    }

    @Test
    void shouldUseJsonFormatter() throws IOException {
        logbook.process(MockHttpRequest.create()).write();

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Precorrelation.class), captor.capture());
        assertThat(captor.getValue()).startsWith("{").endsWith("}");
    }

}
