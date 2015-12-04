package org.zalando.logbook.httpclient;

/*
 * #%L
 * Logbook: HTTP Client
 * %%
 * Copyright (C) 2015 Zalando SE
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.zalando.logbook.BaseHttpRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import static com.google.common.io.ByteStreams.toByteArray;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RequestTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private final Localhost localhost = mock(Localhost.class);

    private HttpRequest get(String uri) {
        return new HttpGet(uri);
    }

    private HttpEntityEnclosingRequest post(final String uri) {
        return new HttpPost(uri);
    }

    private Request unit(final HttpRequest request) {
        return new Request(request, localhost);
    }

    @Test
    public void shouldResolveLocalhost() {
        final Request unit = new Request(get("/"), Localhost.resolve());
        
        assertThat(unit.getRemote(), matchesPattern("(\\d{1,3}\\.){3}\\d{1,3}"));
    }
    
    @Test
    public void shouldHandleUnknownHostException() throws UnknownHostException {
        when(localhost.getAddress()).thenThrow(new UnknownHostException());

        exception.expect(IllegalStateException.class);
        exception.expectCause(instanceOf(UnknownHostException.class));

        unit(get("/")).getRemote();
    }

    @Test
    public void shouldRetrieveAbsoluteRequestUri() {
        final Request unit = unit(get("http://localhost/"));
        assertThat(unit, hasFeature("request uri", BaseHttpRequest::getRequestUri, hasToString("http://localhost/")));
    }

    @Test
    public void shouldRetrieveAbsoluteRequestUriForWrappedRequests() throws URISyntaxException {
        final Request unit = unit(wrap(get("http://localhost/")));

        assertThat(unit, hasFeature("request uri", BaseHttpRequest::getRequestUri, hasToString("http://localhost/")));
    }

    @Test
    public void shouldRetrieveRelativeUriForNonHttpUriRequests() throws URISyntaxException {
        final Request unit = unit(wrap(new BasicHttpRequest("GET", "http://localhost/")));

        assertThat(unit, hasFeature("request uri", BaseHttpRequest::getRequestUri, hasToString("/")));
    }

    private HttpRequestWrapper wrap(HttpRequest delegate) throws URISyntaxException {
        final HttpHost target = HttpHost.create("localhost");
        final HttpRequestWrapper wrap = HttpRequestWrapper.wrap(delegate, target);
        wrap.setURI(URIUtils.rewriteURIForRoute(URI.create("http://localhost/"), new HttpRoute(target)));
        return wrap;
    }

    @Test
    public void shouldReturnContentTypesCharsetIfGiven() {
        final HttpRequest delegate = get("/");
        delegate.addHeader("Content-Type", "text/plain;charset=ISO-8859-1");
        final Request unit = unit(delegate);
        assertThat(unit.getCharset(), is(StandardCharsets.ISO_8859_1));
    }

    @Test
    public void shouldReturnDefaultCharsetIfNoneGiven() {
        final Request unit = unit(get("/"));
        assertThat(unit.getCharset(), is(UTF_8));
    }
    
    @Test
    public void shouldReadBodyIfPresent() throws IOException {
        final HttpEntityEnclosingRequest delegate = post("/");
        delegate.setEntity(new StringEntity("Hello, world!", UTF_8));

        final Request unit = unit(delegate);

        assertThat(new String(unit.withBody().getBody(), UTF_8), is("Hello, world!"));
        assertThat(new String(toByteArray(delegate.getEntity().getContent()), UTF_8), is("Hello, world!"));
    }

}