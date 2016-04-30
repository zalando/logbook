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
import lombok.Builder;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.zalando.logbook.BaseHttpMessage.Headers.copy;

@Immutable
public final class MockRawHttpResponse implements MockHttpMessage, RawHttpResponse {

    private final String protocolVersion;
    private final Origin origin;
    private final int status;
    private final ListMultimap<String, String> headers;
    private final String contentType;
    private final Charset charset;

    @Builder
    public MockRawHttpResponse(
            @Nullable final String protocolVersion,
            @Nullable final Origin origin,
            final int status,
            @Nullable final ListMultimap<String, String> headers,
            @Nullable final String contentType,
            @Nullable final Charset charset) {
        this.protocolVersion = firstNonNull(protocolVersion, "HTTP/1.1");
        this.origin = firstNonNull(origin, Origin.REMOTE);
        this.status = status == 0 ? 200 : status;
        this.headers = copy(firstNonNullNorEmpty(headers, ImmutableListMultimap.of()));
        this.contentType = firstNonNull(contentType, "");
        this.charset = firstNonNull(charset, StandardCharsets.UTF_8);
    }

    @Override
    public String getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public int getStatus() {
        return status;
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
    public Origin getOrigin() {
        return origin;
    }

    @Override
    public HttpResponse withBody() throws IOException {
        return MockHttpResponse.builder()
                .headers(headers)
                .contentType(contentType)
                .charset(charset)
                .origin(origin)
                .status(status)
                .build();
    }

    public static RawHttpResponse create() {
        return builder().build();
    }

}
