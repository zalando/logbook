package org.zalando.logbook.httpclient;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.util.EntityUtils.toByteArray;
import static org.assertj.core.api.Assertions.assertThat;

final class LocalRequestTest {

    private HttpRequest get(final String uri) {
        return new HttpGet(uri);
    }

    private HttpEntityEnclosingRequest post(final String uri) {
        return new HttpPost(uri);
    }

    private HttpContext context(String targetHost) {
        final HttpContext context = HttpCoreContext.create();
        final URI hostUri = URI.create(targetHost);
        final HttpHost httpHost = new HttpHost(hostUri.getHost(), hostUri.getPort(), hostUri.getScheme());
        context.setAttribute(HttpCoreContext.HTTP_TARGET_HOST, httpHost);
        return context;
    }

    private LocalRequest unit(final HttpRequest request) {
        return unit(request, HttpCoreContext.create());
    }

    private LocalRequest unit(final HttpRequest request, final HttpContext context) {
        return new LocalRequest(request, context);
    }

    @Test
    void shouldResolveLocalhost() {
        final LocalRequest unit = unit(get("/"));

        assertThat(unit.getRemote()).isEqualTo("localhost");
    }

    @Test
    void shouldResolveLocalhostWhenContextTargetHostIsUnsupported() {
        final HttpContext context = HttpCoreContext.create();
        context.setAttribute(HttpCoreContext.HTTP_TARGET_HOST, new Object());
        final LocalRequest unit = unit(get("/"), context);

        assertThat(unit.getRemote()).isEqualTo("localhost");
    }

    @Test
    void shouldRetrieveAbsoluteRequestUri() {
        final LocalRequest unit = unit(get("http://localhost/"));

        assertThat(unit.getRequestUri()).hasToString("http://localhost/");
    }

    @Test
    void shouldRetrieveAbsoluteRequestUriFromContext() {
        final LocalRequest unit = unit(get("/test-path?limit=1"), context("https://127.0.0.1:9999"));

        assertThat(unit.getRequestUri()).hasToString("https://127.0.0.1:9999/test-path?limit=1");
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
    void shouldRetrieveAbsoluteRequestUriForWrappedRequests() throws URISyntaxException {
        final LocalRequest unit = unit(wrap(get("http://localhost/")));

        assertThat(unit.getRequestUri()).hasToString("http://localhost/");
    }

    @Test
    void shouldRetrieveRelativeUriForNonHttpUriRequests() {
        final LocalRequest unit = unit(new BasicHttpRequest("GET", "http://localhost/"));

        assertThat(unit.getRequestUri()).hasToString("http://localhost/");
    }

    private HttpRequestWrapper wrap(final HttpRequest delegate) throws URISyntaxException {
        final HttpHost target = HttpHost.create("localhost");
        final HttpRequestWrapper wrap = HttpRequestWrapper.wrap(delegate, target);
        wrap.setURI(URIUtils.rewriteURIForRoute(URI.create("http://localhost/"), new HttpRoute(target)));
        return wrap;
    }

    @Test
    void shouldReturnContentTypesCharsetIfGiven() {
        final HttpRequest delegate = get("/");
        delegate.addHeader("Content-Type", "text/plain;charset=ISO-8859-1");
        final LocalRequest unit = unit(delegate);
        assertThat(unit.getCharset()).isEqualTo(StandardCharsets.ISO_8859_1);
    }

    @Test
    void shouldReturnContentTypeHeader() {
        final HttpRequest delegate = get("/");
        delegate.addHeader("Content-Type", "text/plain;");
        final LocalRequest unit = unit(delegate);
        assertThat(unit.getHeaders()).hasSize(1);
    }

    @Test
    void shouldHandleDuplicateHeaders() {
        final HttpRequest delegate = post("/");
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
        final HttpEntityEnclosingRequest delegate = post("/");
        delegate.setEntity(new StringEntity("Hello, world!", UTF_8));

        final LocalRequest unit = unit(delegate);

        assertThat(new String(unit.withBody().getBody(), UTF_8)).isEqualTo("Hello, world!");
        assertThat(new String(toByteArray(delegate.getEntity()), UTF_8)).isEqualTo("Hello, world!");
    }

    @Test
    void shouldReturnEmptyBodyUntilCaptured() throws IOException {
        final HttpEntityEnclosingRequest delegate = post("/");
        delegate.setEntity(new StringEntity("Hello, world!", UTF_8));

        final LocalRequest unit = unit(delegate);

        assertThat(new String(unit.getBody(), UTF_8)).isEmpty();
        assertThat(new String(unit.withBody().getBody(), UTF_8)).isEqualTo("Hello, world!");
    }

    @Test
    void shouldBeSafeAgainstCallingWithBodyTwice() throws IOException {
        final HttpEntityEnclosingRequest delegate = post("/");
        delegate.setEntity(new StringEntity("Hello, world!", UTF_8));

        final LocalRequest unit = unit(delegate);

        assertThat(new String(unit.withBody().withBody().getBody(), UTF_8)).isEqualTo("Hello, world!");
    }

}
