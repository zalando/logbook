package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.verification.VerificationMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

@LogbookTest(profiles = "deprecated-include", imports = DeprecatedOnlyIncludeTest.Config.class)
class DeprecatedOnlyIncludeTest {

    @Autowired
    private Logbook logbook;

    @MockitoBean
    private HttpLogWriter writer;

    @BeforeEach
    void setUp() {
        doReturn(true).when(writer).isActive();
    }


    @ParameterizedTest
    @CsvSource(value = {
            "'/api/admin', 'GET', false",
            "'/api/admin/users', 'GET', false",
            "'/internal-api', 'GET', false",
            "'/api', 'GET', true",
            "'/api/users', 'GET', true",
            "'/another-api', 'GET', true",
            "'/another-api', 'PUT', true",
            "'/yet-another-api', 'DELETE', false",
            "'/yet-another-api', 'PUT', false",
            "'/yet-another-api', 'GET', false",
            "'/yet-another-api', 'POST', false",
            "'/api', 'DELETE', true",
            "'/api', 'PUT', true",

    })
    void shouldExcludeExpectedRequests(String path, String method, boolean shouldLog) throws IOException {
        logbook.process(request(path).withMethod(method)).write();

        VerificationMode verificationMode = shouldLog ? atLeastOnce() : never();

        verify(writer, verificationMode).write(any(Precorrelation.class), any());
    }

    @Test
    void shouldExcludeRequestWithQueryParameters() throws IOException {
        logbook.process(request("/v2/admin").withQuery("debug=true")).write();

        verify(writer, never()).write(any(Precorrelation.class), any());
    }

    @Test
    void shouldNotExcludeApiWithParameter() throws IOException {
        logbook.process(request("/api").withQuery("debug=true")).write();

        verify(writer).write(any(Precorrelation.class), any());
    }

    private MockHttpRequest request(final String path) {
        return MockHttpRequest.create().withPath(path);
    }

    @TestConfiguration
    public static class Config {
        @Bean
        public Predicate<HttpRequest> condition() {
            return exclude(requestTo("/api/admin/**"));
        }
    }
}
