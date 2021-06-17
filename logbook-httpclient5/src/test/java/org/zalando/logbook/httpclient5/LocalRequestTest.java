package org.zalando.logbook.httpclient5;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

final class LocalRequestTest {

    private ClassicHttpRequest get(final String uri) {
        return new HttpGet(uri);
    }

    private ClassicHttpRequest post(final String uri) {
        return new HttpPost(uri);
    }

    private LocalRequest unit(final ClassicHttpRequest request) {
        return new LocalRequest(request, request.getEntity());
    }

    @Test
    void shouldResolveLocalhost() {
        final LocalRequest unit = unit(get("/"));

        assertThat(unit.getRemote()).isEqualTo("localhost");
    }

    @Test
    void shouldRetrieveAbsoluteRequestUri() {
        final LocalRequest unit = unit(get("http://localhost/"));

        assertThat(unit.getRequestUri()).hasToString("http://localhost/");
    }

    @Test
    void shouldTrimQueryStringFromRequestUri() {
        final LocalRequest unit = unit(get("http://localhost/?limit=1"));

        assertThat(unit.getRequestUri()).hasToString("http://localhost/?limit=1");
    }

    @Test
    void shouldParseQueryStringIntoQueryParameters() {
        final LocalRequest unit = unit(get("http://localhost/?limit=1"));

        assertThat(unit.getQuery()).isEqualTo("limit=1");
    }

    @Test
    void shouldRetrieveRelativeUriForNonHttpUriRequests() {
        final LocalRequest unit = unit(new BasicClassicHttpRequest("GET", "http://localhost/"));

        assertThat(unit.getRequestUri()).hasToString("http://localhost/");
    }

    private ClassicHttpRequest wrap(final ClassicHttpRequest delegate) throws URISyntaxException, UnknownHostException {
        delegate.setUri(URIBuilder.localhost().build());
        return delegate;
    }

    @Test
    void shouldReturnContentTypesCharsetIfGiven() {
        final ClassicHttpRequest delegate = get("/");
        delegate.addHeader("Content-Type", "text/plain;charset=ISO-8859-1");
        final LocalRequest unit = unit(delegate);
        assertThat(unit.getCharset()).isEqualTo(StandardCharsets.ISO_8859_1);
    }

    @Test
    void shouldReturnContentTypeHeader() {
        final ClassicHttpRequest delegate = get("/");
        delegate.addHeader("Content-Type", "text/plain;");
        final LocalRequest unit = unit(delegate);
        assertThat(unit.getHeaders()).hasSize(1);
    }

    @Test
    void shouldHandleDuplicateHeaders() {
        final ClassicHttpRequest delegate = post("/");
        delegate.addHeader("Content-Type", "text/plain;");
        delegate.addHeader("Content-Type", "text/plain;");
        final LocalRequest unit = unit(delegate);

        assertThat(unit.getHeaders())
                .hasSize(1)
                .hasEntrySatisfying("Content-Type", values -> assertThat(values).hasSize(2));
    }

    @Test
    void shouldReturnDefaultCharsetIfNoneGiven() {
        final LocalRequest unit = unit(get("/"));
        assertThat(unit.getCharset()).isEqualTo(UTF_8);
    }

    @Test
    void shouldReadBodyIfPresent() throws IOException {
        final ClassicHttpRequest delegate = post("/");
        delegate.setEntity(new StringEntity("Hello, world!", UTF_8));

        final LocalRequest unit = unit(delegate);

        assertThat(new String(unit.withBody().getBody(), UTF_8)).isEqualTo("Hello, world!");
        assertThat(new String(EntityUtils.toByteArray(delegate.getEntity()), UTF_8)).isEqualTo("Hello, world!");
    }

    @Test
    void shouldReturnEmptyBodyUntilCaptured() throws IOException {
        final ClassicHttpRequest delegate = post("/");
        delegate.setEntity(new StringEntity("Hello, world!", UTF_8));

        final LocalRequest unit = unit(delegate);

        assertThat(new String(unit.getBody(), UTF_8)).isEmpty();
        assertThat(new String(unit.withBody().getBody(), UTF_8)).isEqualTo("Hello, world!");
    }

    @Test
    void shouldBeSafeAgainstCallingWithBodyTwice() throws IOException {
        final ClassicHttpRequest delegate = post("/");
        delegate.setEntity(new StringEntity("Hello, world!", UTF_8));

        final LocalRequest unit = unit(delegate);

        assertThat(new String(unit.withBody().withBody().getBody(), UTF_8)).isEqualTo("Hello, world!");
    }

    @Test
    void shouldResolveProtocol() {
        ClassicHttpRequest request = get("/");
        request.setVersion(HttpVersion.HTTP_1_0);
        final LocalRequest unit = unit(request);

        assertThat(unit.getProtocolVersion()).isEqualTo(HttpVersion.HTTP_1_0.toString());
    }

}
