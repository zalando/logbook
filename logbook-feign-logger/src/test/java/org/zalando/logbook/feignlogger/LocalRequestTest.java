package org.zalando.logbook.feignlogger;


import org.junit.jupiter.api.Test;
import org.zalando.logbook.BaseHttpRequest;
import org.zalando.logbook.feignlogger.LocalRequest;
import org.zalando.logbook.feignlogger.Localhost;

import feign.Request;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
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

    private Request get(final String uri) {
        return Request.create("GET", uri, Collections.emptyMap(), null, StandardCharsets.UTF_8);
    }

    private Request post(final String uri) {
        return Request.create("POST", uri, Collections.emptyMap(), null, StandardCharsets.UTF_8);
    }

    private LocalRequest unit(final Request request) {
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
    void shouldRetrieveRelativeUriForNonHttpUriRequests() throws URISyntaxException {
        final LocalRequest unit = unit(get("http://localhost/"));

        assertThat(unit, hasFeature("request uri", BaseHttpRequest::getRequestUri, hasToString("http://localhost/")));
    }


    @Test
    void shouldReturnContentTypesCharsetIfGiven() {
        final LocalRequest unit = unit(Request.create("GET", "/", Collections.emptyMap(), null, StandardCharsets.ISO_8859_1));
        assertThat(unit.getCharset(), is(StandardCharsets.ISO_8859_1));
    }

    @Test
    void shouldReturnContentTypeHeader() {
        Map<String, Collection<String>> headers = new HashMap<String, Collection<String>>();
        headers.put("Content-Type", Arrays.asList("text/plain"));

        final LocalRequest unit = unit(Request.create("GET", "/", headers, null, null));

        assertThat(unit.getHeaders(), aMapWithSize(1));
    }

    @Test
    void shouldReturnDefaultCharsetIfNoneGiven() {
        final LocalRequest unit = unit(get("/"));
        assertThat(unit.getCharset(), is(UTF_8));
    }

    @Test
    void shouldReadBodyIfPresent() throws IOException {
        final LocalRequest unit = unit(Request.create("POST", "/", Collections.emptyMap(), "Hello, world!".getBytes(StandardCharsets.UTF_8), null));
        
        assertThat(new String(unit.withBody().getBody(), UTF_8), is("Hello, world!"));
    }

}
