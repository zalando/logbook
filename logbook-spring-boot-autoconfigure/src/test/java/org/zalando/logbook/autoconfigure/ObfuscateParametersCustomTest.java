package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.zalando.logbook.MockHttpRequest;
import org.zalando.logbook.api.HttpLogWriter;
import org.zalando.logbook.api.HttpRequest;
import org.zalando.logbook.api.Logbook;
import org.zalando.logbook.api.Precorrelation;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@LogbookTest(profiles = "parameters")
class ObfuscateParametersCustomTest {

    @Autowired
    private Logbook logbook;

    @MockBean
    private HttpLogWriter writer;

    @BeforeEach
    void setUp() {
        doReturn(true).when(writer).isActive();
    }

    @Test
    void shouldFilterParameters() throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withQuery("access_token=s3cr3t&q=logbook");

        logbook.process(request).write();

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Precorrelation.class), captor.capture());
        final String message = captor.getValue();

        assertThat(message).contains("access_token=s3cr3t&q=XXX");
    }

}
