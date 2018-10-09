package org.zalando.logbook.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.Correlator;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.MockHttpResponse;
import org.zalando.logbook.MockRawHttpRequest;
import org.zalando.logbook.MockRawHttpResponse;
import org.zalando.logbook.ResponseFilter;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@LogbookTest(properties = "logbook.format.style = http")
class ObfuscateResponseCustomTest {

    @Autowired
    private Logbook logbook;

    @MockBean
    private HttpLogWriter writer;

    @MockBean
    private ResponseFilter responseFilter;

    @Captor
    private ArgumentCaptor<Correlation<String, String>> captor;

    @BeforeEach
    void setUp() throws IOException {
        doReturn(true).when(writer).isActive(any());
        doReturn(MockHttpResponse.create().withBodyAsString("<secret>")).when(responseFilter).filter(any());
    }

    @Test
    void shouldFilterResponseBody() throws IOException {
        final Optional<Correlator> correlator = logbook.write(MockRawHttpRequest.create());

        correlator.get().write(MockRawHttpResponse.create()
                .withBodyAsString("Hello"));

        verify(writer).writeResponse(captor.capture());
        final String message = captor.getValue().getResponse();

        assertThat(message, not(containsString("Hello")));
        assertThat(message, containsString("<secret>"));
    }

}
