package org.zalando.logbook.spring;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.MockRawHttpRequest;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;
import java.util.function.Function;

import static org.hamcrest.Matchers.containsString;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

@SpringBootTest(
        classes = {Application.class, FormatStyleCurlTest.TestConfiguration.class},
        properties = "logbook.format.style = curl")
public final class FormatStyleCurlTest extends AbstractTest {

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
    void shouldUseHttpFormatter() throws IOException {
        logbook.write(MockRawHttpRequest.create());

        verify(writer).writeRequest(argThat(isCurlFormatter()));
    }

    private Matcher<Precorrelation<String>> isCurlFormatter() {
        final Function<Precorrelation<String>, String> getRequest = Precorrelation::getRequest;
        return hasFeature("request", getRequest, containsString("curl -v -X GET 'http://localhost/'"));
    }

}
