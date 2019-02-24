package org.zalando.logbook.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.MockHttpRequest;

import java.io.IOException;
import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.zalando.logbook.Conditions.exclude;
import static org.zalando.logbook.Conditions.requestTo;

@LogbookTest(profiles = "include", imports = IncludeTest.Config.class)
class IncludeTest {

    @TestConfiguration
    public static class Config {
        @Bean
        public Predicate<HttpRequest> condition() {
            return exclude(requestTo("/api/admin/**"));
        }
    }

    @Autowired
    private Logbook logbook;

    @MockBean
    private Logger logger;

    @BeforeEach
    void setUp() {
        doReturn(true).when(logger).isTraceEnabled();
    }

    @Test
    void shouldExcludeAdmin() throws IOException {
        logbook.process(request("/api/admin")).write();

        verify(logger, never()).trace(any());
    }

    @Test
    void shouldExcludeAdminWithPath() throws IOException {
        logbook.process(request("/api/admin/users")).write();

        verify(logger, never()).trace(any());
    }

    @Test
    void shouldExcludeAdminWithQueryParameters() throws IOException {
        logbook.process(request("/api/admin").withQuery("debug=true")).write();

        verify(logger, never()).trace(any());
    }

    @Test
    void shouldExcludeInternalApi() throws IOException {
        logbook.process(request("/internal-api")).write();

        verify(logger, never()).trace(any());
    }

    @Test
    void shouldExcludeInternalApiWithPath() throws IOException {
        logbook.process(request("/internal-api/users")).write();

        verify(logger, never()).trace(any());
    }

    @Test
    void shouldExcludeInternalApiWithQueryParameters() throws IOException {
        logbook.process(request("/internal-api").withQuery("debug=true")).write();

        verify(logger, never()).trace(any());
    }

    @Test
    void shouldNotExcludeApi() throws IOException {
        logbook.process(request("/api")).write();

        verify(logger).trace(any());
    }

    @Test
    void shouldNotExcludeApiWithPath() throws IOException {
        logbook.process(request("/api/users")).write();

        verify(logger).trace(any());
    }

    @Test
    void shouldNotExcludeApiWithParameter() throws IOException {
        logbook.process(request("/api").withQuery("debug=true")).write();

        verify(logger).trace(any());
    }

    private MockHttpRequest request(final String path) {
        return MockHttpRequest.create().withPath(path);
    }

}
