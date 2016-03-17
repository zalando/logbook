package org.zalando.logbook.spring;

/*
 * #%L
 * Logbook: Core
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

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import lombok.Builder;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.zalando.logbook.Origin.REMOTE;

public final class MockHttpRequest implements HttpRequest {

    private final Origin origin;
    private final String remote;
    private final String method;
    private final String requestUri;
    private final Multimap<String, String> headers;
    private final String contentType;
    private final Charset charset;
    private final String body;

    @Builder
    public MockHttpRequest(@Nullable final Origin origin,
            @Nullable final String remote,
            @Nullable final String method,
            @Nullable final String requestUri,
            @Nullable final Multimap<String, String> headers,
            @Nullable final String contentType,
            @Nullable final Charset charset,
            @Nullable final String body) {
        this.origin = firstNonNull(origin, REMOTE);
        this.remote = firstNonNull(remote, "127.0.0.1");
        this.method = firstNonNull(method, "GET");
        this.requestUri = firstNonNull(requestUri, "/");
        this.headers = firstNonNullNorEmpty(headers, ImmutableMultimap.of());
        this.contentType = firstNonNull(contentType, "");
        this.charset = firstNonNull(charset, StandardCharsets.UTF_8);
        this.body = firstNonNull(body, "");
    }

    static <K, V> Multimap<K, V> firstNonNullNorEmpty(@Nullable final Multimap<K, V> first, final Multimap<K, V> second) {
        return first != null && !first.isEmpty() ? first : checkNotNull(second);
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
    public String getRequestUri() {
        return requestUri;
    }

    @Override
    public Multimap<String, String> getHeaders() {
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
        return builder().build();
    }

}
