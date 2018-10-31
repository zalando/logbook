package org.zalando.logbook.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.MockHttpRequest;
import org.zalando.logbook.MockHttpResponse;
import org.zalando.logbook.ResponseFilter;

import java.io.IOException;

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

    @BeforeEach
    void setUp() {
        doReturn(true).when(writer).isActive();
        doReturn(MockHttpResponse.create().withBodyAsString("<secret>")).when(responseFilter).filter(any());
    }

    @Test
    void shouldFilterResponseBody() throws IOException {
        logbook.process(MockHttpRequest.create()).write()
        .process(MockHttpResponse.create()
                .withBodyAsString("Hello")).write();

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Correlation.class), captor.capture());
        final String message = captor.getValue();

        assertThat(message, not(containsString("Hello")));
        assertThat(message, containsString("<secret>"));
    }

}
