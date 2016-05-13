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
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import static org.zalando.logbook.NullSafe.firstNonNull;

@Immutable
public final class MockRawHttpResponse implements MockHttpMessage, RawHttpResponse {

    private final String protocolVersion;
    private final Origin origin;
    private final int status;
    private final Map<String, List<String>> headers;
    private final String contentType;
    private final Charset charset;
    private final String body;

    @lombok.Builder(builderMethodName = "response", builderClassName = "Builder")
    public MockRawHttpResponse(
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
    public HttpResponse withBody() throws IOException {
        return MockHttpResponse.response()
                .protocolVersion(protocolVersion)
                .origin(origin)
                .status(status)
                .headers(headers)
                .contentType(contentType)
                .charset(charset)
                .body(body)
                .build();
    }

    public static RawHttpResponse create() {
        return response().build();
    }

}
