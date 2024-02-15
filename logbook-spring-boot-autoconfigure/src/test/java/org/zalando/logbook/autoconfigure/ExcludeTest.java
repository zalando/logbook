package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.verification.VerificationMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.zalando.logbook.*;
import org.zalando.logbook.test.MockHttpRequest;

import java.io.IOException;
import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.zalando.logbook.core.Conditions.*;


class ExcludeTest {

    @Nested
    @LogbookTest(profiles = "exclude", imports = ExcludeTest.PathConfig.class)
    class ExcludePathTest {

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
                "'/health', 'GET', false",
                "'/admin', 'GET', false",
                "'/admin/users', 'GET', false",
                "'/another-api', 'DELETE', false",
                "'/another-api', 'PUT', false",
                "'/another-api', 'GET', true",
                "'/yet-another-api', 'GET', false",
                "'/yet-another-api', 'PUT', false",
                "'/yet-another-api', 'POST', false",
                "'/api', 'GET', true",
                "'/api', 'DELETE', false",
                "'/admin', 'PUT', false",
                "'/some/path', 'GET', true",
        })
        void shouldExcludeExpectedRequests(String path, String method, boolean shouldLog) throws IOException {
            logbook.process(request(path).withMethod(method)).write();

            VerificationMode verificationMode = shouldLog ? atLeastOnce() : never();

            verify(writer, verificationMode).write(any(Precorrelation.class), any());
        }

        @Test
        void shouldExcludeAdminWithQueryParameters() throws IOException {
            logbook.process(MockHttpRequest.create()
                    .withPath("/admin")
                    .withQuery("debug=true")).write();

            verify(writer, never()).write(any(Precorrelation.class), any());
        }
    }

    @Nested
    @LogbookTest(imports = ExcludeTest.HeaderConfig.class)
    class ExcludeHeaderTest {

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
                "'host', 'localhost', false",
                "'host', '127.0.0.1', true",
                "'host', '', true",
                "'localhost', 'localhost', true",
        })
        void shouldExcludeExpectedRequests(String key, String value, boolean shouldLog) throws IOException {
            logbook.process(request("/health").withHeaders(HttpHeaders.of(key, value))).write();

            VerificationMode verificationMode = shouldLog ? atLeastOnce() : never();

            verify(writer, verificationMode).write(any(Precorrelation.class), any());
        }
    }

    @Nested
    @LogbookTest(imports = ExcludeTest.WildcardHeaderConfig.class)
    class ExcludeWildcardHeaderTest {

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
                "'host', 'localhost', false",
                "'host', '127.0.0.1', false",
                "'host', '', false",
                "'localhost', 'localhost', true",
                "'localhost', '', true",
        })
        void shouldExcludeExpectedRequests(String key, String value, boolean shouldLog) throws IOException {
            logbook.process(request("/health").withHeaders(HttpHeaders.of(key, value))).write();

            VerificationMode verificationMode = shouldLog ? atLeastOnce() : never();

            verify(writer, verificationMode).write(any(Precorrelation.class), any());
        }
    }

    private MockHttpRequest request(final String path) {
        return MockHttpRequest.create().withPath(path);
    }

    @TestConfiguration
    static class PathConfig {
        @Bean
        public Predicate<HttpRequest> requestCondition() {
            return exclude(requestTo("/health"));
        }
    }

    @TestConfiguration
    static class HeaderConfig {
        @Bean
        public Predicate<HttpRequest> requestCondition() {
            return exclude(conditionalHeader("host", "localhost"));
        }
    }

    @TestConfiguration
    static class WildcardHeaderConfig {
        @Bean
        public Predicate<HttpRequest> requestCondition() {
            return exclude(conditionalHeader("host", "*"));
        }
    }
}
