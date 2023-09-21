package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.verification.VerificationMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.test.MockHttpRequest;

import java.io.IOException;
import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.zalando.logbook.core.Conditions.exclude;
import static org.zalando.logbook.core.Conditions.requestTo;


@LogbookTest(profiles = "deprecated-exclude", imports = DeprecatedOnlyExcludeTest.Config.class)
class DeprecatedOnlyExcludeTest {

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
            "'/another-api', 'GET', false",
            "'/yet-another-api', 'GET', true",
            "'/yet-another-api', 'PUT', true",
            "'/yet-another-api', 'POST', true",
            "'/api', 'GET', true",
            "'/api', 'DELETE', true",
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

    private MockHttpRequest request(final String path) {
        return MockHttpRequest.create().withPath(path);
    }

    @TestConfiguration
    static class Config {
        @Bean
        public Predicate<HttpRequest> requestCondition() {
            return exclude(requestTo("/health"));
        }
    }

}
