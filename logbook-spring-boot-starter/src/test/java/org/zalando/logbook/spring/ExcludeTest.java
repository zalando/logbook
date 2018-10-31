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

@LogbookTest(profiles = "exclude", imports = ExcludeTest.Config.class)
class ExcludeTest {

    @TestConfiguration
    public static class Config {
        @Bean
        public Predicate<HttpRequest> requestCondition() {
            return exclude(requestTo("/health"));
        }
    }

    @Autowired
    private Logbook logbook;

    @MockBean(name = "httpLogger")
    private Logger logger;

    @BeforeEach
    void setUp() {
        doReturn(true).when(logger).isTraceEnabled();
    }

    @Test
    void shouldExcludeHealth() throws IOException {
        logbook.process(request("/health")).write();

        verify(logger, never()).trace(any());
    }

    @Test
    void shouldExcludeAdmin() throws IOException {
        logbook.process(request("/admin")).write();

        verify(logger, never()).trace(any());
    }

    @Test
    void shouldExcludeAdminWithPath() throws IOException {
        logbook.process(request("/admin/users")).write();

        verify(logger, never()).trace(any());
    }

    @Test
    void shouldExcludeAdminWithQueryParameters() throws IOException {
        logbook.process(MockHttpRequest.create()
                .withPath("/admin")
                .withQuery("debug=true")).write();

        verify(logger, never()).trace(any());
    }

    @Test
    void shouldNotExcludeApi() throws IOException {
        logbook.process(request("/admin/api")).write();

        verify(logger).trace(any());
    }

    private MockHttpRequest request(final String path) {
        return MockHttpRequest.create().withPath(path);
    }

}
