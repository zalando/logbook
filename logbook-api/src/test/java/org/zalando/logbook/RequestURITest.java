package org.zalando.logbook;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        Assertions.assertThat(RequestURI.reconstruct(request, RequestURI.Component.AUTHORITY, RequestURI.Component.PATH, RequestURI.Component.QUERY)).isEqualTo("//localhost/admin?limit=1");

    }

    @Test
    void shouldReconstructWithoutAuthority() {
        Assertions.assertThat(RequestURI.reconstruct(request, RequestURI.Component.SCHEME, RequestURI.Component.PATH, RequestURI.Component.QUERY)).isEqualTo("http:///admin?limit=1");
    }

    @Test
    void shouldReconstructWithoutSchemeAndAuthority() {
        Assertions.assertThat(RequestURI.reconstruct(request, RequestURI.Component.PATH, RequestURI.Component.QUERY)).isEqualTo("/admin?limit=1");
    }

    @Test
    void shouldReconstructWithoutPath() {
        Assertions.assertThat(RequestURI.reconstruct(request, RequestURI.Component.SCHEME, RequestURI.Component.AUTHORITY, RequestURI.Component.QUERY)).isEqualTo("http://localhost/?limit=1");
    }

    @Test
    void shouldReconstructWithNullPath() {
        when(request.getPath()).thenReturn(null);
        Assertions.assertThat(RequestURI.reconstruct(request)).isEqualTo("http://localhost/?limit=1");
    }

    @Test
    void shouldReconstructWithNonDefaultPortAndPathWithoutSlash() {
        when(request.getPath()).thenReturn("admin");
        when(request.getPort()).thenReturn(Optional.of(1556));
        Assertions.assertThat(RequestURI.reconstruct(request)).isEqualTo("http://localhost:1556/admin?limit=1");
    }

    @Test
    void shouldReconstructWithoutQuery() {
        Assertions.assertThat(RequestURI.reconstruct(request, RequestURI.Component.SCHEME, RequestURI.Component.AUTHORITY, RequestURI.Component.PATH)).isEqualTo("http://localhost/admin");
    }

    @Test
    void shouldReconstructWithoutEmptyQuery() {
        when(request.getQuery()).thenReturn("");

        assertThat(request.getRequestUri()).isEqualTo("http://localhost/admin");
    }

    @Test
    void shouldReconstructWithoutSchemeAuthorityAndPath() {
        Assertions.assertThat(RequestURI.reconstruct(request, RequestURI.Component.QUERY)).isEqualTo("/?limit=1");
    }

    @Test
    void shouldUseComponentValueOf() {
        RequestURI.Component.valueOf("SCHEME");
    }

    @Test
    void shouldUseOriginValueOf() {
        Origin.valueOf("LOCAL");
    }

    @Test
    void shouldReconstructUsingBuilder() {
        final StringBuilder builder = new StringBuilder();
        RequestURI.reconstruct(request, builder);
        assertThat(builder.toString()).isEqualTo("http://localhost/admin?limit=1");
    }

    @Test
    void shouldReconstructSpecificComponents() {
        final String r = RequestURI.reconstruct(request, EnumSet.of(RequestURI.Component.SCHEME, RequestURI.Component.AUTHORITY, RequestURI.Component.PATH));
        assertThat(r).isEqualTo("http://localhost/admin");
    }
}
