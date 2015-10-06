package org.zalando.logbook.spring;

/*
 * #%L
 * Logbook: Spring
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.zalando.logbook.RawHttpRequest;

import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

final class Request implements RawHttpRequest, org.zalando.logbook.HttpRequest {

    private final HttpRequest request;
    private final byte[] body;
    private final Localhost localhost;

    Request(final HttpRequest request, final byte[] body, final Localhost localhost) {
        this.request = request;
        this.body = body;
        this.localhost = localhost;
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
        return request.getMethod().name();
    }

    @Override
    public URI getRequestUri() {
        return request.getURI();
    }

    @Override
    public Multimap<String, String> getHeaders() {
        final ListMultimap<String, String> map = ArrayListMultimap.create();
        request.getHeaders().forEach(map::putAll);
        return map;
    }

    @Override
    public String getContentType() {
        return Objects.toString(request.getHeaders().getContentType(), null);
    }

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(request)
                .map(HttpRequest::getHeaders)
                .map(HttpHeaders::getContentType)
                .map(MediaType::getCharSet)
                .orElse(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    @Override
    public org.zalando.logbook.HttpRequest withBody() {
        return this;
    }

}
