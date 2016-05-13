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

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.MoreObjects.firstNonNull;

@Immutable
public final class MockHttpResponse implements MockHttpMessage, HttpResponse {

    private final String protocolVersion;
    private final Origin origin;
    private final int status;
    private final Map<String, List<String>> headers;
    private final String contentType;
    private final Charset charset;
    private final String body;

    @lombok.Builder(builderMethodName = "response", builderClassName = "Builder")
    public MockHttpResponse(
            @Nullable final String protocolVersion,
            @Nullable final Origin origin,
            final int status,
            @Nullable final Map<String, List<String>> headers,
            @Nullable final String contentType,
            @Nullable final Charset charset,
            @Nullable final String body) {
        this.protocolVersion = firstNonNull(protocolVersion, "HTTP/1.1");
        this.origin = firstNonNull(origin, Origin.LOCAL);
        this.status = status == 0 ? 200 : status;
        this.headers = firstNonNullNorEmpty(headers, Collections.emptyMap());
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
    public int getStatus() {
        return status;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
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
        return response().build();
    }

}
