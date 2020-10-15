package org.zalando.logbook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.RequestURI.Component;

import java.util.EnumSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.RequestURI.Component.AUTHORITY;
import static org.zalando.logbook.RequestURI.Component.PATH;
import static org.zalando.logbook.RequestURI.Component.QUERY;
import static org.zalando.logbook.RequestURI.Component.SCHEME;
import static org.zalando.logbook.RequestURI.reconstruct;

final class RequestURITest {

    private final HttpRequest request = mock(HttpRequest.class);

    @BeforeEach
    void setUp() {
        when(request.getRequestUri()).thenCallRealMethod();
        when(request.getScheme()).thenReturn("http");
        when(request.getHost()).thenReturn("localhost");
        when(request.getPort()).thenReturn(Optional.empty());
        when(request.getPath()).thenReturn("/admin");
        when(request.getQuery()).thenReturn("limit=1");
    }

    @Test
    void shouldReconstructFully() {
        assertThat(request.getRequestUri()).isEqualTo("http://localhost/admin?limit=1");
    }

    @Test
    void shouldNotIncludeStandardHttpPort() {
        when(request.getScheme()).thenReturn("http");
        when(request.getPort()).thenReturn(Optional.of(80));
        assertThat(request.getRequestUri()).isEqualTo("http://localhost/admin?limit=1");
    }

    @Test
    void shouldNotIncludeStandardHttpsPort() {
        when(request.getScheme()).thenReturn("https");
        when(request.getPort()).thenReturn(Optional.of(443));
        assertThat(request.getRequestUri()).isEqualTo("https://localhost/admin?limit=1");
    }

    @Test
    void shouldIncludeNonStandardHttpPort() {
        when(request.getPort()).thenReturn(Optional.of(8080));
        assertThat(request.getRequestUri()).isEqualTo("http://localhost:8080/admin?limit=1");
    }

    @Test
    void shouldIncludeNonStandardHttpsPort() {
        when(request.getScheme()).thenReturn("https");
        when(request.getPort()).thenReturn(Optional.of(1443));
        assertThat(request.getRequestUri()).isEqualTo("https://localhost:1443/admin?limit=1");
    }

    @Test
    void shouldReconstructWithoutSchema() {
        assertThat(reconstruct(request, AUTHORITY, PATH, QUERY)).isEqualTo("//localhost/admin?limit=1");

    }

    @Test
    void shouldReconstructWithoutAuthority() {
        assertThat(reconstruct(request, SCHEME, PATH, QUERY)).isEqualTo("http:///admin?limit=1");
    }

    @Test
    void shouldReconstructWithoutSchemeAndAuthority() {
        assertThat(reconstruct(request, PATH, QUERY)).isEqualTo("/admin?limit=1");
    }

    @Test
    void shouldReconstructWithoutPath() {
        assertThat(reconstruct(request, SCHEME, AUTHORITY, QUERY)).isEqualTo("http://localhost/?limit=1");
    }

    @Test
    void shouldReconstructWithoutQuery() {
        assertThat(reconstruct(request, SCHEME, AUTHORITY, PATH)).isEqualTo("http://localhost/admin");
    }

    @Test
    void shouldReconstructWithoutEmptyQuery() {
        when(request.getQuery()).thenReturn("");

        assertThat(request.getRequestUri()).isEqualTo("http://localhost/admin");
    }

    @Test
    void shouldReconstructWithoutSchemeAuthorityAndPath() {
        assertThat(reconstruct(request, QUERY)).isEqualTo("/?limit=1");
    }

    @Test
    void shouldUseComponentValueOf() {
        Component.valueOf("SCHEME");
    }

    @Test
    void shouldUseOriginValueOf() {
        Origin.valueOf("LOCAL");
    }

    @Test
    void shouldReconstructUsingBuilder() {
        final StringBuilder builder = new StringBuilder();
        reconstruct(request, builder);
        assertThat(builder.toString()).isEqualTo("http://localhost/admin?limit=1");
    }

    @Test
    void shouldReconstructSpecificComponents() {
        final String r = reconstruct(request, EnumSet.of(SCHEME, AUTHORITY, PATH));
        assertThat(r).isEqualTo("http://localhost/admin");
    }
}
