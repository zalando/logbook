package org.zalando.logbook.servlet;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
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

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.time.Duration;

import static javax.servlet.DispatcherType.ASYNC;
import static javax.servlet.DispatcherType.REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
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

    @Autowired
    private FilterRegistrationBean<SpyingHttpFilter> spyingHttpFilterReg;

    static class AsyncHttpServlet extends HttpServlet{
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
            final AsyncContext asyncCtx = req.startAsync();
            if ("flushBrokenPipe".equals(req.getQueryString())) {
                try {
                    doThrow(new IOException("Broken Pipe")).when(((LocalResponse) resp).getResponse()).flushBuffer();
                } catch (IOException ignored) {
                }
            }
            asyncResponse(asyncCtx);
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp)  {
            asyncResponse(req.startAsync());
        }

        private void asyncResponse(AsyncContext asyncContext) {
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                    final HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
                    response.setStatus(200);
                    response.addHeader("Content-Type", "text/plain");
                    response.getWriter().println("Hello Async");
                    asyncContext.complete();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }

    static class SpyingHttpFilter implements HttpFilter {
        public HttpServletRequest latestSpiedRequest;
        public HttpServletResponse latestSpiedResponse;

        @Override
        public void doFilter(HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain chain) throws ServletException, IOException {
            latestSpiedRequest = spy(new HttpServletRequestWrapper(httpRequest));
            latestSpiedResponse = spy(new HttpServletResponseWrapper(httpResponse));
            chain.doFilter(latestSpiedRequest, latestSpiedResponse);
        }
    }

    @Configuration
    @Import(ExampleController.class)
    static class TestConfiguration {

        @Bean
        public ServletRegistrationBean<AsyncHttpServlet> asyncHttpServlet() {
            ServletRegistrationBean<AsyncHttpServlet> bean = new ServletRegistrationBean<>(new AsyncHttpServlet(), "/servlet/*");
            bean.setLoadOnStartup(1);
            return bean;
        }

        @Bean
        public Logbook logbook(final HttpLogWriter writer) {
            return Logbook.builder()
                    .sink(new DefaultSink(
                            new DefaultHttpLogFormatter(), writer))
                    .build();
        }

        @Bean
        public FilterRegistrationBean<SpyingHttpFilter> spyingHttpFilter() {
            final FilterRegistrationBean<SpyingHttpFilter> registration = new FilterRegistrationBean<>(new SpyingHttpFilter());
            registration.setOrder(Integer.MAX_VALUE);
            registration.setDispatcherTypes(REQUEST, ASYNC);
            return registration;
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

        waitFor(Duration.ofSeconds(1));

        final String request = interceptRequest();

        assertThat(request)
                .contains("127.0.0.1", "GET", "http://localhost:8080/api/async");
    }

    @Test
    void shouldFormatAsyncResponse() throws Exception {
        final RestTemplate template = new RestTemplate();
        template.getForObject("http://localhost:8080/api/async", String.class);

        waitFor(Duration.ofSeconds(1));

        final String response = interceptResponse();

        assertThat(response)
                .contains("200 OK", "text/plain", "Hello, world!");
    }

    @Test
    void shouldFormatAsyncServletResponseGet() throws Exception {
        final RestTemplate template = new RestTemplate();
        template.getForObject("http://localhost:8080/servlet/async", String.class);

        waitFor(Duration.ofSeconds(1));

        interceptRequest();
        final String response = interceptResponse();

        assertThat(response)
                .contains("200 OK", "text/plain", "Hello Async");
    }

    @Test
    void shouldFormatAsyncServletResponsePost() throws Exception {
        final RestTemplate template = new RestTemplate();
        template.postForObject("http://localhost:8080/servlet/async", "Hello Servlet", String.class);

        waitFor(Duration.ofSeconds(1));

        interceptRequest();
        final String response = interceptResponse();

        assertThat(response)
                .contains("200 OK", "text/plain", "Hello Async");
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "?flushBrokenPipe" })
    void shouldGracefullyHandleFlushBufferIssuesOnAsyncServletResponse(String urlQuery) throws Exception {
        final RestTemplate template = new RestTemplate();
        template.getForObject("http://localhost:8080/servlet/async" + urlQuery, String.class);

        waitFor(Duration.ofSeconds(1));

        HttpServletResponse httpResp = spyingHttpFilterReg.getFilter().latestSpiedResponse;
        final String response = interceptResponse();

        verify(httpResp, times(1)).flushBuffer();
        assertThat(response)
            .contains("200 OK", "text/plain", "Hello Async");
    }

    @SneakyThrows
    private void waitFor(final Duration duration) {
        Thread.sleep(duration.toMillis());
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
