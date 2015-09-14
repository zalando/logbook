package org.zalando.springframework.web.logging;

/*
 * #%L
 * spring-web-logging
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

import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.list;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class LogDataBuilder {

    private final Obfuscator headerObfuscator;

    private final Obfuscator parameterObfuscator;

    private final Obfuscator bodyObfuscator;

    private final boolean includePayload;

    public LogDataBuilder() {
        this(true);
    }

    public LogDataBuilder(final boolean includePayload) {
        this(new NullObfuscator(), new NullObfuscator(), new NullObfuscator(), includePayload);
    }

    public LogDataBuilder(final Obfuscator headerObfuscator, final Obfuscator parameterObfuscator,
            final Obfuscator bodyObfuscator) {
        this(headerObfuscator, parameterObfuscator, bodyObfuscator, true);
    }

    public LogDataBuilder(final Obfuscator headerObfuscator, final Obfuscator parameterObfuscator,
            final Obfuscator bodyObfuscator, final boolean includePayload) {
        this.headerObfuscator = headerObfuscator;
        this.parameterObfuscator = parameterObfuscator;
        this.bodyObfuscator = bodyObfuscator;
        this.includePayload = includePayload;
    }

    public RequestData buildRequest(final HttpServletRequest request) {
        final String remote = request.getRemoteAddr();

        final String method = request.getMethod();

        final String uri = request.getRequestURL().toString();

        final Map<String, List<String>> headers = list(request.getHeaderNames())
                .stream()
                .collect(toMap(h -> h, h -> list(request.getHeaders(h))
                        .stream()
                        .map(v -> headerObfuscator.obfuscate(h, v))
                        .collect(toList())));

        final Map<String, List<String>> parameters =
                request.getParameterMap().entrySet()
                        .stream()
                        .collect(toMap(Map.Entry::getKey, entry -> asList(entry.getValue())
                                .stream()
                                .map(v -> parameterObfuscator.obfuscate(entry.getKey(), v))
                                .collect(toList())));

        final String body = bodyObfuscator.obfuscate("body", payload(request));
        return new RequestData(remote, method, uri, headers, parameters, body);
    }

    public ResponseData buildResponse(final HttpServletResponse response) {
        final int status = response.getStatus();

        final Map<String, List<String>> headers = (response.getHeaderNames())
                .stream()
                .collect(toMap(h -> h, h -> singletonList(headerObfuscator.obfuscate(h, response.getHeader(h)))));

        final String contentType = response.getContentType();

        return new ResponseData(status, headers, contentType, payload(response));
    }

    private String payload(final HttpServletRequest request) {
        if (!includePayload) {
            return "<payload is not included>";
        }

        if (request instanceof ConsumingHttpServletRequestWrapper) {
            return payload(((ConsumingHttpServletRequestWrapper) request).getContentAsByteArray(), request.getCharacterEncoding());
        } else {
            return "<payload is not consumable>";
        }
    }

    private String payload(final HttpServletResponse response) {
        if (!includePayload) {
            return "<payload is not included>";
        }

        if (response instanceof ContentCachingResponseWrapper) {
            return payload(((ContentCachingResponseWrapper) response).getContentAsByteArray(), response.getCharacterEncoding());
        } else {
            return "<payload is not consumable>";
        }
    }

    private String payload(final byte[] buffer, final String encoding) {
        final Charset charset;
        if (encoding != null && Charset.isSupported(encoding)) {
            charset = Charset.forName(encoding);
        } else {
            charset = Charset.defaultCharset();
        }
        return new String(buffer, 0, buffer.length, charset);
    }
}
