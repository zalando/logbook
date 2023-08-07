package org.zalando.logbook.autoconfigure;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.test.MockHttpRequest;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import static ch.qos.logback.classic.Level.TRACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.zalando.logbook.core.Conditions.exclude;
import static org.zalando.logbook.core.Conditions.requestTo;

@LogbookTest(profiles = "include", imports = IncludeTest.Config.class)
class IncludeTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(Logbook.class);

    private final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    private final List<ILoggingEvent> logsList = listAppender.list;

    {
        logger.setLevel(TRACE);
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @TestConfiguration
    public static class Config {
        @Bean
        public Predicate<HttpRequest> condition() {
            return exclude(requestTo("/api/admin/**"));
        }
    }

    @Autowired
    private Logbook logbook;

    @BeforeEach
    void setUp() {
        logsList.clear();
    }


    @ParameterizedTest
    @CsvSource(value = {
            "'/api/admin', 'GET', false",
            "'/api/admin/users', 'GET', false",
            "'/internal-api', 'GET', false",
            "'/internal-api/users', 'GET', false",
            "'/api', 'GET', true",
            "'/api/users', 'GET', true",
            "'/another-api', 'GET', true",
            "'/another-api', 'PUT', true",
            "'/another-api', 'POST', false",
            "'/another-api', 'DELETE', false",
            "'/api', 'DELETE', false",

    })
    void shouldExcludeExpectedRequests(String path, String method, boolean shouldLog) throws IOException {
        logbook.process(request(path).withMethod(method)).write();

        assertThat(logsList).hasSize(shouldLog ? 1 : 0);
    }

    @Test
    void shouldExcludeAdminWithQueryParameters() throws IOException {
        logbook.process(request("/api/admin").withQuery("debug=true")).write();

        assertThat(logsList).isEmpty();
    }

    @Test
    void shouldExcludeInternalApiWithQueryParameters() throws IOException {
        logbook.process(request("/internal-api").withQuery("debug=true")).write();

        assertThat(logsList).isEmpty();
    }

    @Test
    void shouldNotExcludeApiWithParameter() throws IOException {
        logbook.process(request("/api").withQuery("debug=true")).write();

        assertThat(logsList).hasSize(1);
    }

    private MockHttpRequest request(final String path) {
        return MockHttpRequest.create().withPath(path);
    }

}
