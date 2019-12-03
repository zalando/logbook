package org.zalando.logbook.servlet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.DefaultSink;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;

import javax.servlet.DispatcherType;
import java.io.IOException;

import static javax.servlet.DispatcherType.ASYNC;
import static javax.servlet.DispatcherType.REQUEST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

/**
 * Verifies that {@link LogbookFilter} handles {@link DispatcherType#ASYNC} correctly.
 */
@SpringBootTest(webEnvironment = DEFINED_PORT)
@EnableAutoConfiguration(exclude = ErrorMvcAutoConfiguration.class)
final class AsyncDispatchTest {

    @MockBean
    private HttpLogWriter writer;

    @Configuration
    @Import(ExampleController.class)
    static class TestConfiguration {

        @Bean
        public Logbook logbook(final HttpLogWriter writer) {
            return Logbook.builder()
                    .sink(new DefaultSink(
                            new DefaultHttpLogFormatter(), writer))
                    .build();
        }


        @Bean
        @SuppressWarnings({"rawtypes", "unchecked"}) // as of Spring Boot 2.x
        public FilterRegistrationBean logbookFilter(final Logbook logbook) {
            final FilterRegistrationBean registration =
                    new FilterRegistrationBean(new LogbookFilter(logbook));
            registration.setDispatcherTypes(REQUEST, ASYNC);
            return registration;
        }

    }

    @BeforeEach
    void setUp() {
        when(writer.isActive()).thenReturn(true);
    }

    @Test
    void shouldFormatAsyncRequest() throws Exception {
        final RestTemplate template = new RestTemplate();
        template.getForObject("http://localhost:8080/api/async", String.class);

        final String request = interceptRequest();

        assertThat(request, containsString("127.0.0.1"));
        assertThat(request, containsString("GET"));
        assertThat(request, containsString("http://localhost:8080/api/async"));
    }

    @Test
    void shouldFormatAsyncResponse() throws Exception {
        final RestTemplate template = new RestTemplate();
        template.getForObject("http://localhost:8080/api/async", String.class);

        final String response = interceptResponse();

        assertThat(response, containsString("200 OK"));
        assertThat(response, containsString("text/plain"));
        assertThat(response, containsString("Hello, world!"));
    }

    private String interceptRequest() throws IOException {
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Precorrelation.class), captor.capture());
        return captor.getValue();
    }

    private String interceptResponse() throws IOException {
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Correlation.class), captor.capture());
        return captor.getValue();
    }

}
