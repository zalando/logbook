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

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.zalando.logbook.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

final class Request implements RawHttpRequest, org.zalando.logbook.HttpRequest {

    private final HttpRequest request;
    private final Localhost localhost;

    private byte[] body;

    Request(final HttpRequest request, final Localhost localhost) {
        this.request = request;
        this.localhost = localhost;
    }

    @Override
    public Origin getOrigin() {
        return Origin.LOCAL;
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
    public String getRequestUri() {
        final HttpRequest original = request instanceof HttpRequestWrapper ?
                HttpRequestWrapper.class.cast(request).getOriginal() :
                request;

        return original instanceof HttpUriRequest ?
                HttpUriRequest.class.cast(original).getURI().toASCIIString():
                request.getRequestLine().getUri();
    }

    @Override
    public Multimap<String, String> getHeaders() {
        final Multimap<String, String> map = Multimaps.immutableOf();

        for (Header header : request.getAllHeaders()) {
            map.putValue(header.getName(), header.getValue());
        }

        return map;
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
                .orElse(StandardCharsets.UTF_8);
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
            this.body = ByteStreamUtils.toByteArray(content);
            foo.setEntity(new ByteArrayEntity(body));
        } else {
            this.body = new byte[0];
        }

        return this;
    }

}
