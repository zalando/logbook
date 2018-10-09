package org.zalando.logbook.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.MockHeaders;
import org.zalando.logbook.MockRawHttpRequest;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.RawHttpRequest;

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

    @Captor
    private ArgumentCaptor<Precorrelation<String>> captor;

    @BeforeEach
    void setUp() throws IOException {
        doReturn(true).when(writer).isActive(any());
    }

    @Test
    void shouldFilterHeaders() throws IOException {
        final RawHttpRequest request = MockRawHttpRequest.create()
                .withHeaders(MockHeaders.of(
                        "Authorization", "123",
                        "X-Access-Token", "123",
                        "X-Trace-ID", "ABC"
                ));

        logbook.write(request);

        verify(writer).writeRequest(captor.capture());
        final String message = captor.getValue().getRequest();

        assertThat(message, containsString("Authorization: XXX"));
        assertThat(message, containsString("X-Access-Token: XXX"));
        assertThat(message, containsString("X-Trace-ID: ABC"));
    }

}
