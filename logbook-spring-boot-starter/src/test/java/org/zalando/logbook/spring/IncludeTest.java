package org.zalando.logbook.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.MockRawHttpRequest;
import org.zalando.logbook.RawHttpRequest;

import java.io.IOException;
import java.util.function.Predicate;

import static java.util.Optional.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.doReturn;
import static org.zalando.logbook.Conditions.exclude;
import static org.zalando.logbook.Conditions.requestTo;

@LogbookTest(profiles = "include", imports = IncludeTest.Config.class)
class IncludeTest {

    @TestConfiguration
    public static class Config {
        @Bean
        public Predicate<RawHttpRequest> condition() {
            return exclude(requestTo("/api/admin/**"));
        }
    }

    @Autowired
    private Logbook logbook;

    @MockBean
    private Logger httpLogger;

    @BeforeEach
    void setUp() {
        doReturn(true).when(httpLogger).isTraceEnabled();
    }

    @Test
    void shouldExcludeAdmin() throws IOException {
        assertThat(logbook.write(request("/api/admin")), is(empty()));
    }

    @Test
    void shouldExcludeAdminWithPath() throws IOException {
        assertThat(logbook.write(request("/api/admin/users")), is(empty()));
    }

    @Test
    void shouldExcludeAdminWithQueryParameters() throws IOException {
        assertThat(logbook.write(request("/api/admin").withQuery("debug=true")), is(empty()));
    }

    @Test
    void shouldExcludeInternalApi() throws IOException {
        assertThat(logbook.write(request("/internal-api")), is(empty()));
    }

    @Test
    void shouldExcludeInternalApiWithPath() throws IOException {
        assertThat(logbook.write(request("/internal-api/users")), is(empty()));
    }

    @Test
    void shouldExcludeInternalApiWithQueryParameters() throws IOException {
        assertThat(logbook.write(request("/internal-api").withQuery("debug=true")), is(empty()));
    }

    @Test
    void shouldNotExcludeApi() throws IOException {
        assertThat(logbook.write(request("/api")), is(not(empty())));
    }

    @Test
    void shouldNotExcludeApiWithPath() throws IOException {
        assertThat(logbook.write(request("/api/users")), is(not(empty())));
    }

    @Test
    void shouldNotExcludeApiWithParameter() throws IOException {
        assertThat(logbook.write(request("/api").withQuery("debug=true")), is(not(empty())));
    }

    private MockRawHttpRequest request(final String path) {
        return MockRawHttpRequest.create().withPath(path);
    }

}
