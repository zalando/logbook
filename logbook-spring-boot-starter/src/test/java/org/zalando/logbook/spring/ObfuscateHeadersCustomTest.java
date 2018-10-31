package org.zalando.logbook.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.MockHeaders;
import org.zalando.logbook.MockHttpRequest;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@LogbookTest(profiles = "headers")
class ObfuscateHeadersCustomTest {

    @Autowired
    private Logbook logbook;

    @MockBean
    private HttpLogWriter writer;

    @BeforeEach
    void setUp() {
        doReturn(true).when(writer).isActive();
    }

    @Test
    void shouldFilterHeaders() throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withHeaders(MockHeaders.of(
                        "Authorization", "123",
                        "X-Access-Token", "123",
                        "X-Trace-ID", "ABC"
                ));

        logbook.process(request).write();

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(), captor.capture());
        final String message = captor.getValue();

        assertThat(message, containsString("Authorization: XXX"));
        assertThat(message, containsString("X-Access-Token: XXX"));
        assertThat(message, containsString("X-Trace-ID: ABC"));
    }

}
