package org.zalando.logbook.autoconfigure;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.verification.VerificationMode;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.zalando.logbook.*;
import org.zalando.logbook.test.MockHttpRequest;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import static ch.qos.logback.classic.Level.TRACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.zalando.logbook.core.Conditions.*;

class IncludeTest {

    @Nested
    @LogbookTest(profiles = "include", imports = IncludeTest.PathConfig.class)
    class IncludePathTest {

        private final Logger logger = (Logger) LoggerFactory.getLogger(Logbook.class);

        private final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        private final List<ILoggingEvent> logsList = listAppender.list;

        {
            logger.setLevel(TRACE);
            listAppender.start();
            logger.addAppender(listAppender);
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
                "'/another-api', 'POST', true",
                "'/another-api', 'DELETE', true",
                "'/yet-another-api', 'DELETE', true",
                "'/yet-another-api', 'PUT', true",
                "'/yet-another-api', 'GET', false",
                "'/yet-another-api', 'POST', false",
                "'/api', 'DELETE', true",
                "'/api', 'PUT', true",

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

    }

    @Nested
    @LogbookTest(imports = IncludeTest.HeaderConfig.class)
    class IncludeHeaderTest {

        @Autowired
        private Logbook logbook;

        @MockBean
        private HttpLogWriter writer;

        @BeforeEach
        void setUp() {
            doReturn(true).when(writer).isActive();
        }


        @ParameterizedTest
        @CsvSource(value = {
                "'host', 'localhost', true",
                "'host', '127.0.0.1', false",
                "'host', '', false",
                "'localhost', 'localhost', false",
        })
        void shouldExcludeExpectedRequests(String key, String value, boolean shouldLog) throws IOException {
            logbook.process(request("/api").withHeaders(HttpHeaders.of(key, value))).write();

            VerificationMode verificationMode = shouldLog ? atLeastOnce() : never();

            verify(writer, verificationMode).write(any(Precorrelation.class), any());
        }

    }

    @Nested
    @LogbookTest(imports = IncludeTest.WildcardHeaderConfig.class)
    class IncludeWildcardHeaderTest {

        @Autowired
        private Logbook logbook;

        @MockBean
        private HttpLogWriter writer;

        @BeforeEach
        void setUp() {
            doReturn(true).when(writer).isActive();
        }


        @ParameterizedTest
        @CsvSource(value = {
                "'host', 'localhost', true",
                "'host', '127.0.0.1', true",
                "'host', '', true",
                "'hosts', 'localhost', false",
                "'hosts', '', false",
        })
        void shouldExcludeExpectedRequests(String key, String value, boolean shouldLog) throws IOException {
            logbook.process(request("/api").withHeaders(HttpHeaders.of(key, value))).write();

            VerificationMode verificationMode = shouldLog ? atLeastOnce() : never();

            verify(writer, verificationMode).write(any(Precorrelation.class), any());
        }

    }

    private MockHttpRequest request(final String path) {
        return MockHttpRequest.create().withPath(path);
    }

    @TestConfiguration
    public static class PathConfig {
        @Bean
        public Predicate<HttpRequest> condition() {
            return exclude(requestTo("/api/admin/**"));
        }
    }

    @TestConfiguration
    public static class HeaderConfig {
        @Bean
        public Predicate<HttpRequest> condition() {
            return conditionalHeader("host", "localhost");
        }
    }

    @TestConfiguration
    static class WildcardHeaderConfig {
        @Bean
        public Predicate<HttpRequest> requestCondition() {
            return conditionalHeader("host", "*");
        }
    }
}
