package org.zalando.logbook.autoconfigure;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.test.MockHttpRequest;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.io.IOException;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zalando.logbook.core.Conditions.exclude;
import static org.zalando.logbook.core.Conditions.requestTo;
import static uk.org.lidalia.slf4jext.Level.TRACE;

@LogbookTest(profiles = "include", imports = IncludeTest.Config.class)
class IncludeTest {

    private final TestLogger logger = TestLoggerFactory.getTestLogger(Logbook.class);

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
        logger.setEnabledLevels(TRACE);
    }

    @BeforeEach
    @AfterEach
    void cleanUp() {
        logger.clear();
    }

    @Test
    void shouldExcludeAdmin() throws IOException {
        logbook.process(request("/api/admin")).write();

        assertThat(logger.getLoggingEvents()).isEmpty();
    }

    @Test
    void shouldExcludeAdminWithPath() throws IOException {
        logbook.process(request("/api/admin/users")).write();

        assertThat(logger.getLoggingEvents()).isEmpty();
    }

    @Test
    void shouldExcludeAdminWithQueryParameters() throws IOException {
        logbook.process(request("/api/admin").withQuery("debug=true")).write();

        assertThat(logger.getLoggingEvents()).isEmpty();
    }

    @Test
    void shouldExcludeInternalApi() throws IOException {
        logbook.process(request("/internal-api")).write();

        assertThat(logger.getLoggingEvents()).isEmpty();
    }

    @Test
    void shouldExcludeInternalApiWithPath() throws IOException {
        logbook.process(request("/internal-api/users")).write();

        assertThat(logger.getLoggingEvents()).isEmpty();
    }

    @Test
    void shouldExcludeInternalApiWithQueryParameters() throws IOException {
        logbook.process(request("/internal-api").withQuery("debug=true")).write();

        assertThat(logger.getLoggingEvents()).isEmpty();
    }

    @Test
    void shouldNotExcludeApi() throws IOException {
        logbook.process(request("/api")).write();

        final ImmutableList<LoggingEvent> events = logger.getLoggingEvents();
        assertThat(events).hasSize(1);
    }

    @Test
    void shouldNotExcludeApiWithPath() throws IOException {
        logbook.process(request("/api/users")).write();

        final ImmutableList<LoggingEvent> events = logger.getLoggingEvents();
        assertThat(events).hasSize(1);
    }

    @Test
    void shouldNotExcludeApiWithParameter() throws IOException {
        logbook.process(request("/api").withQuery("debug=true")).write();

        final ImmutableList<LoggingEvent> events = logger.getLoggingEvents();
        assertThat(events).hasSize(1);
    }

    private MockHttpRequest request(final String path) {
        return MockHttpRequest.create().withPath(path);
    }

}
