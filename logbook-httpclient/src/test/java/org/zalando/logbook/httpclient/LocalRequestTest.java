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
import org.junit.jupiter.api.Test;
import org.zalando.logbook.BaseHttpRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.util.EntityUtils.toByteArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class LocalRequestTest {

    private final Localhost localhost = mock(Localhost.class);

    private HttpRequest get(final String uri) {
        return new HttpGet(uri);
    }

    private HttpEntityEnclosingRequest post(final String uri) {
        return new HttpPost(uri);
    }

    private LocalRequest unit(final HttpRequest request) {
        return new LocalRequest(request, localhost);
    }

    @Test
    void shouldResolveLocalhost() {
        final LocalRequest unit = new LocalRequest(get("/"), Localhost.resolve());

        assertThat(unit.getRemote(), matchesPattern("(\\d{1,3}\\.){3}\\d{1,3}"));
    }

    @Test
    void shouldHandleUnknownHostException() throws UnknownHostException {
        final LocalRequest unit = new LocalRequest(get("/"), localhost);
        when(localhost.getAddress()).thenThrow(new UnknownHostException());

        assertThat(unit.getRemote(), unit(get("/")).getRemote(), matchesPattern("(\\d{1,3}\\.){3}\\d{1,3}"));
    }

    @Test
    void shouldRetrieveAbsoluteRequestUri() {
        final LocalRequest unit = unit(get("http://localhost/"));
        assertThat(unit, hasFeature("request uri", BaseHttpRequest::getRequestUri, hasToString("http://localhost/")));
    }

    @Test
    void shouldTrimQueryStringFromRequestUri() {
        final LocalRequest unit = unit(get("http://localhost/?limit=1"));

        assertThat(unit, hasFeature("request uri", BaseHttpRequest::getRequestUri,
                hasToString("http://localhost/?limit=1")));
    }

    @Test
    void shouldParseQueryStringIntoQueryParameters() {
        final LocalRequest unit = unit(get("http://localhost/?limit=1"));

        assertThat(unit, hasFeature("query parameters", BaseHttpRequest::getQuery, is("limit=1")));
    }

    @Test
    void shouldRetrieveAbsoluteRequestUriForWrappedRequests() throws URISyntaxException {
        final LocalRequest unit = unit(wrap(get("http://localhost/")));

        assertThat(unit, hasFeature("request uri", BaseHttpRequest::getRequestUri, hasToString("http://localhost/")));
    }

    @Test
    void shouldRetrieveRelativeUriForNonHttpUriRequests() throws URISyntaxException {
        final LocalRequest unit = unit(new BasicHttpRequest("GET", "http://localhost/"));

        assertThat(unit, hasFeature("request uri", BaseHttpRequest::getRequestUri, hasToString("http://localhost/")));
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
        assertThat(unit.getCharset(), is(StandardCharsets.ISO_8859_1));
    }

    @Test
    void shouldReturnContentTypeHeader() {
        final HttpRequest delegate = get("/");
        delegate.addHeader("Content-Type", "text/plain;");
        final LocalRequest unit = unit(delegate);
        assertThat(unit.getHeaders(), aMapWithSize(1));
    }

    @Test
    void shouldHandleDuplicateHeaders() {
        final HttpRequest delegate = post("/");
        delegate.addHeader("Content-Type", "text/plain;");
        delegate.addHeader("Content-Type", "text/plain;");
        final LocalRequest unit = unit(delegate);
        assertThat(unit.getHeaders(), aMapWithSize(1));
        assertThat(unit.getHeaders().get("Content-Type"), hasSize(2));
    }

    @Test
    void shouldReturnDefaultCharsetIfNoneGiven() {
        final LocalRequest unit = unit(get("/"));
        assertThat(unit.getCharset(), is(UTF_8));
    }

    @Test
    void shouldReadBodyIfPresent() throws IOException {
        final HttpEntityEnclosingRequest delegate = post("/");
        delegate.setEntity(new StringEntity("Hello, world!", UTF_8));

        final LocalRequest unit = unit(delegate);

        assertThat(new String(unit.withBody().getBody(), UTF_8), is("Hello, world!"));
        assertThat(new String(toByteArray(delegate.getEntity()), UTF_8), is("Hello, world!"));
    }

}
