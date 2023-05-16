package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.zalando.logbook.core.Conditions.exclude;
import static org.zalando.logbook.core.Conditions.requestTo;

@TestConfiguration
class Config {
    @Bean
    public Predicate<HttpRequest> requestCondition() {
        return exclude(requestTo("/health"));
    }
}

@LogbookTest(profiles = "exclude", imports = Config.class)
class ExcludeTest {

    @Autowired
    private Logbook logbook;

    @MockBean
    private HttpLogWriter writer;

    @BeforeEach
    void setUp() {
        doReturn(true).when(writer).isActive();
    }

    @Test
    void shouldExcludeHealth() throws IOException {
        logbook.process(request("/health")).write();

        verify(writer, never()).write(any(Precorrelation.class), any());
    }

    @Test
    void shouldExcludeAdmin() throws IOException {
        logbook.process(request("/admin")).write();

        verify(writer, never()).write(any(Precorrelation.class), any());
    }

    @Test
    void shouldExcludeAdminWithPath() throws IOException {
        logbook.process(request("/admin/users")).write();

        verify(writer, never()).write(any(Precorrelation.class), any());
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

}
