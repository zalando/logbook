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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.io.ByteStreams;
import lombok.SneakyThrows;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.zalando.logbook.Origin;
import org.zalando.logbook.RawHttpRequest;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Optional;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.client.utils.URLEncodedUtils.parse;

final class LocalRequest implements RawHttpRequest, org.zalando.logbook.HttpRequest {

    private final HttpRequest request;
    private final Localhost localhost;
    private final URI originalRequestUri;

    private byte[] body;

    LocalRequest(final HttpRequest request, final Localhost localhost) {
        this.request = request;
        this.localhost = localhost;
        this.originalRequestUri = getOriginalRequestUri(request);
    }

    private static URI getOriginalRequestUri(final HttpRequest request) {
        return request instanceof HttpRequestWrapper ?
                getUri(HttpRequestWrapper.class.cast(request)):
                request instanceof HttpUriRequest ?
                HttpUriRequest.class.cast(request).getURI() :
                URI.create(request.getRequestLine().getUri()); // TODO dumb fallback
    }

    private static URI getUri(final HttpRequestWrapper request) {

        return URI.create(request.getOriginal().getRequestLine().getUri());
    }

    @Override
    public Origin getOrigin() {
        return Origin.LOCAL;
    }

    @Override
    public String getProtocolVersion() {
        return request.getRequestLine().getProtocolVersion().toString();
    }

    @Override
    public String getRemote() {
        try {
            return localhost.getAddress();
        } catch (final UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getMethod() {
        return request.getRequestLine().getMethod();
    }


    @Override
    public String getScheme() {
        return originalRequestUri.getScheme();
    }

    @Override
    public String getHost() {
        return originalRequestUri.getHost();
    }

    @Override
    public int getPort() {
        final int port = originalRequestUri.getPort();
        return port == -1 ? 80 : port; // TODO(wschoenborn): is that safe to do?
    }

    @Override
    public String getPath() {
        return originalRequestUri.getPath();
    }

    @Override
    public String getQuery() {
        return firstNonNull(originalRequestUri.getQuery(), "");
    }

    @SneakyThrows
    @VisibleForTesting
    static String stripQueryString(final String scheme, final String userInfo, final String host, final int port,
            final String path, final String fragment) {
        return new URI(scheme, userInfo, host, port, path, null, fragment).toASCIIString();
    }

    @Override
    public ListMultimap<String, String> getQueryParameters() {
        final ListMultimap<String, String> parameters = ArrayListMultimap.create();

        @Nullable final String query = originalRequestUri.getRawQuery();

        if (query == null) {
            return ImmutableListMultimap.of();
        }

        for (final NameValuePair pair : parse(query, UTF_8)) {
            parameters.put(pair.getName(), pair.getValue());
        }

        return Multimaps.unmodifiableListMultimap(parameters);
    }

    @Override
    public ListMultimap<String, String> getHeaders() {
        final ListMultimap<String, String> headers = Headers.create();

        for (final Header header : request.getAllHeaders()) {
            headers.put(header.getName(), header.getValue());
        }

        return Multimaps.unmodifiableListMultimap(headers);
    }

    @Override
    public String getContentType() {
        return Optional.of(request)
                .map(request -> request.getFirstHeader("Content-Type"))
                .map(Header::getValue)
                .orElse("");
    }

    @Override
    public Charset getCharset() {
        return Optional.of(request)
                .map(request -> request.getFirstHeader("Content-Type"))
                .map(Header::getValue)
                .map(ContentType::parse)
                .map(ContentType::getCharset)
                .orElse(UTF_8);
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    @Override
    public org.zalando.logbook.HttpRequest withBody() throws IOException {
        if (request instanceof HttpEntityEnclosingRequest) {
            final HttpEntityEnclosingRequest foo = (HttpEntityEnclosingRequest) request;
            final InputStream content = foo.getEntity().getContent();
            this.body = ByteStreams.toByteArray(content);
            foo.setEntity(new ByteArrayEntity(body));
        } else {
            this.body = new byte[0];
        }

        return this;
    }

}
