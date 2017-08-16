package org.zalando.logbook.spring;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = {Application.class, ObfuscateResponseCustomTest.TestConfiguration.class},
        properties = "logbook.format.style = http")
public final class ObfuscateResponseCustomTest extends AbstractTest {

    @Configuration
    public static class TestConfiguration {

        @Bean
        public HttpLogWriter writer() throws IOException {
            final HttpLogWriter writer = mock(HttpLogWriter.class);
            when(writer.isActive(any())).thenReturn(true);
            return writer;
        }

        @Bean
        public ResponseFilter responseFilter() {
            return request -> MockHttpResponse.create()
                    .withBodyAsString("<secret>");
        }

    }

    @Autowired
    private Logbook logbook;

    @Autowired
    private HttpLogWriter writer;

    @Test
    void shouldFilterResponseBody() throws IOException {
        final Optional<Correlator> correlator = logbook.write(MockRawHttpRequest.create());

        correlator.get().write(MockRawHttpResponse.create()
                .withBodyAsString("Hello"));

        final ArgumentCaptor<Correlation<String, String>> captor = newCaptor();
        verify(writer).writeResponse(captor.capture());
        final String message = captor.getValue().getResponse();

        assertThat(message, not(containsString("Hello")));
        assertThat(message, containsString("<secret>"));
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<Correlation<String, String>> newCaptor() {
        return ArgumentCaptor.forClass(Correlation.class);
    }

}
