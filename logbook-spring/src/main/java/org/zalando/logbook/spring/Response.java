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
import com.google.common.io.ByteStreams;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.RawHttpResponse;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

final class Response implements RawHttpResponse, HttpResponse {

    private final ClientHttpResponse response;
    private byte[] body = new byte[0];

    Response(final ClientHttpResponse response) {
        this.response = response;
    }

    @Override
    public int getStatus() {
        try {
            return response.getRawStatusCode();
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Multimap<String, String> getHeaders() {
        final ListMultimap<String, String> map = ArrayListMultimap.create();
        response.getHeaders().forEach(map::putAll);
        return map;
    }

    @Override
    public String getContentType() {
        return Objects.toString(response.getHeaders().getContentType(), null);
    }

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(response)
                .map(ClientHttpResponse::getHeaders)
                .map(HttpHeaders::getContentType)
                .map(MediaType::getCharSet)
                .orElse(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    @Override
    public HttpResponse withBody() throws IOException {
        @Nullable final InputStream stream = response.getBody();

        if (stream != null) {
            this.body = ByteStreams.toByteArray(stream);
        }

        return this;
    }

    ClientHttpResponse asClientHttpResponse() {
        return new ForwardingClientHttpResponse() {
            @Override
            protected ClientHttpResponse delegate() {
                return response;
            }

            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(body);
            }
        };
    }

}
