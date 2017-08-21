package org.zalando.logbook.spring;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = {Application.class, ObfuscateHeadersDefaultTest.TestConfiguration.class},
        properties = "logbook.format.style = http")
public final class ObfuscateHeadersDefaultTest extends AbstractTest {

    @Configuration
    public static class TestConfiguration {

        @Bean
        public HttpLogWriter writer() throws IOException {
            final HttpLogWriter writer = mock(HttpLogWriter.class);
            when(writer.isActive(any())).thenReturn(true);
            return writer;
        }

    }

    @Autowired
    private Logbook logbook;

    @Autowired
    private HttpLogWriter writer;

    @Test
    void shouldFilterAuthorizationByDefault() throws IOException {
        final RawHttpRequest request = MockRawHttpRequest.create()
                .withHeaders(MockHeaders.of(
                        "Authorization", "123",
                        "X-Secret", "123"
                ));

        logbook.write(request);

        final ArgumentCaptor<Precorrelation<String>> captor = newCaptor();
        verify(writer).writeRequest(captor.capture());
        final String message = captor.getValue().getRequest();

        assertThat(message, containsString("Authorization: XXX"));
        assertThat(message, containsString("X-Secret: 123"));
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<Precorrelation<String>> newCaptor() {
        return ArgumentCaptor.forClass(Precorrelation.class);
    }

}
