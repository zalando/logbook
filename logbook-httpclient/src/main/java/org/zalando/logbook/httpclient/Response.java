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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteStreams;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.zalando.logbook.RawHttpResponse;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

final class Response implements RawHttpResponse, org.zalando.logbook.HttpResponse {

    private final HttpResponse response;
    private byte[] body;

    Response(final HttpResponse response) {
        this.response = response;
    }

    @Override
    public int getStatus() {
        return response.getStatusLine().getStatusCode();
    }

    @Override
    public Multimap<String, String> getHeaders() {
        final ListMultimap<String, String> map = ArrayListMultimap.create();

        for (Header header : response.getAllHeaders()) {
            map.put(header.getName(), header.getValue());
        }

        return map;
    }

    @Override
    public String getContentType() {
        return Optional.of(response)
                .map(request -> request.getFirstHeader("Content-Type"))
                .map(Header::getValue)
                .orElse("");
    }

    @Override
    public Charset getCharset() {
        return Optional.of(response)
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
    public org.zalando.logbook.HttpResponse withBody() throws IOException {
        @Nullable final HttpEntity entity = response.getEntity();
        
        if (entity == null) {
            this.body = new byte[0];
            return this;
        }
        
        this.body = ByteStreams.toByteArray(entity.getContent());
        response.setEntity(new ByteArrayEntity(body));
        
        return this;
    }

}
