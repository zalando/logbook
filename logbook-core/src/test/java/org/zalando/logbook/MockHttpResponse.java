package org.zalando.logbook;

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


import lombok.Builder;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.zalando.logbook.MockHttpRequest.firstNonNullNorEmpty;
import static org.zalando.logbook.Util.firstNonNull;

public final class MockHttpResponse implements HttpResponse {

    private final Origin origin;
    private final int status;
    private final Multimap<String, String> headers;
    private final String contentType;
    private final Charset charset;
    private final String body;

    @Builder
    public MockHttpResponse(@Nullable final Origin origin, 
            final int status,
            @Nullable final Multimap<String, String> headers,
            @Nullable final String contentType,
            @Nullable final Charset charset,
            @Nullable final String body) {
        this.origin = firstNonNull(origin, Origin.LOCAL);
        this.status = status == 0 ? 200 : status;
        this.headers = firstNonNullNorEmpty(headers, Multimaps.immutableOf());
        this.contentType = firstNonNull(contentType, "");
        this.charset = firstNonNull(charset, StandardCharsets.UTF_8);
        this.body = firstNonNull(body, "");
    }

    @Override
    public Origin getOrigin() {
        return origin;
    }

    @Override
    public int getStatus() {
        return status;
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

    static HttpResponse create() {
        return builder().build();
    }

}
