package org.zalando.logbook;

/*
 * #%L
 * Logbook: Test
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

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.zalando.logbook.MockHeaders.copy;

@Immutable
public final class MockHttpRequest implements MockHttpMessage, HttpRequest {

    private final String protocolVersion;
    private final Origin origin;
    private final String remote;
    private final String method;
    private final String scheme;
    private final String host;
    private final int port;
    private final String path;
    private final String query;
    private final ListMultimap<String, String> headers;
    private final String contentType;
    private final Charset charset;
    private final String body;

    @lombok.Builder(builderMethodName = "request", builderClassName = "Builder")
    public MockHttpRequest(
            @Nullable final String protocolVersion,
            @Nullable final Origin origin,
            @Nullable final String remote,
            @Nullable final String method,
            @Nullable final String scheme,
            @Nullable final String host,
            final int port,
            @Nullable final String path,
            @Nullable final String query,
            @Nullable final ListMultimap<String, String> headers,
            @Nullable final String contentType,
            @Nullable final Charset charset,
            @Nullable final String body) {
        this.protocolVersion = firstNonNull(protocolVersion, "HTTP/1.1");
        this.origin = firstNonNull(origin, Origin.REMOTE);
        this.remote = firstNonNull(remote, "127.0.0.1");
        this.method = firstNonNull(method, "GET");
        this.scheme = firstNonNull(scheme, "http");
        this.host = firstNonNull(host, "localhost");
        this.port = port == 0 ? 80 : port;
        this.path = firstNonNull(path, "/");
        this.query = firstNonNull(query, "");
        this.headers = copy(firstNonNullNorEmpty(headers, ImmutableListMultimap.of()));
        this.contentType = firstNonNull(contentType, "");
        this.charset = firstNonNull(charset, StandardCharsets.UTF_8);
        this.body = firstNonNull(body, "");
    }

    @Override
    public String getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public Origin getOrigin() {
        return origin;
    }

    @Override
    public String getRemote() {
        return remote;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public ListMultimap<String, String> getHeaders() {
        return headers;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public byte[] getBody() {
        return getBodyAsString().getBytes(getCharset());
    }

    @Override
    public String getBodyAsString() {
        return body;
    }

    static HttpRequest create() {
        return request().build();
    }

}
