package org.zalando.logbook.spring;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.zalando.logbook.BodyObfuscator;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.MockRawHttpRequest;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.RawHttpRequest;

import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration
@TestPropertySource(properties = "logbook.format.style = http")
public final class ObfuscateBodyCustomTest extends AbstractTest {

    @Configuration
    public static class TestConfiguration {

        @Bean
        public HttpLogWriter writer() throws IOException {
            final HttpLogWriter writer = mock(HttpLogWriter.class);
            when(writer.isActive(any())).thenReturn(true);
            return writer;
        }

        @Bean
        public BodyObfuscator bodyObfuscator() {
            return (contentType, body) -> "<secret>";
        }

    }

    @Autowired
    private Logbook logbook;

    @Autowired
    private HttpLogWriter writer;

    @Test
    public void shouldObfuscateRequestBody() throws IOException {
        final RawHttpRequest request = MockRawHttpRequest.request()
                .body("Hello")
                .build();

        logbook.write(request);

        final ArgumentCaptor<Precorrelation<String>> captor = newCaptor();
        verify(writer).writeRequest(captor.capture());
        final String message = captor.getValue().getRequest();

        assertThat(message, not(containsString("Hello")));
        assertThat(message, containsString("<secret>"));
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<Precorrelation<String>> newCaptor() {
        return ArgumentCaptor.forClass(Precorrelation.class);
    }

}