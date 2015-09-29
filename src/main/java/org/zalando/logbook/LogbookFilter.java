package org.zalando.logbook;

/*
 * #%L
 * Logbook
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
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.google.common.io.ByteStreams.toByteArray;

public final class LogbookFilter extends OnceFilter {

    static final String REQUEST_ATTRIBUTE_NAME = "logbook.request-body";
    static final String RESPONSE_ATTRIBUTE_NAME = "logbook.response-body";

    private final HttpLogFormatter formatter;
    private final HttpLogWriter writer;

    public LogbookFilter() {
        this(new DefaultHttpLogFormatter(), new DefaultHttpLogWriter());
    }

    public LogbookFilter(final HttpLogFormatter formatter, final HttpLogWriter writer) {
        this.formatter = formatter;
        this.writer = writer;
    }

    @Override
    public boolean skipAsyncDispatch() {
        return false;
    }

    @Override
    public boolean skipErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilter(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain chain) throws ServletException, IOException {

        if (writer.isActive(request, response)) {
            filter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    private void filter(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
            final FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest request = prepareAndLog(httpRequest);
        final TeeHttpServletResponse response = prepare(httpRequest, httpResponse);

        try {
            chain.doFilter(request, response);
            finishResponse(httpRequest, response);
        } finally {
            logResponse(httpRequest, response);
        }
    }

    private HttpServletRequest prepareAndLog(final HttpServletRequest request) throws IOException {
        if (isAsyncDispatch(request)) {
            return request;
        } else {
            final byte[] content = retrieveContent(request);
            final TeeHttpServletRequest teeRequest = new DefaultTeeHttpServletRequest(request, content);
            teeRequest.setAttribute(REQUEST_ATTRIBUTE_NAME, content);

            writer.writeRequest(formatter.format(teeRequest));

            return teeRequest;
        }
    }

    private byte[] retrieveContent(final HttpServletRequest request) throws IOException {
        @Nullable final byte[] content = (byte[]) request.getAttribute(REQUEST_ATTRIBUTE_NAME);
        return content == null ? toByteArray(request.getInputStream()) : content;
    }

    private TeeHttpServletResponse prepare(final HttpServletRequest request, final HttpServletResponse response) {
        return new DefaultTeeHttpServletResponse(response, () ->
                (byte[]) request.getAttribute(RESPONSE_ATTRIBUTE_NAME));
    }

    private void finishResponse(final HttpServletRequest request, final TeeHttpServletResponse response) throws IOException {
        if (isNormalResponseOrLastResponseOfAsyncDispatch(request)) {
            request.setAttribute(RESPONSE_ATTRIBUTE_NAME, response.getBodyAsByteArray());
            response.finish();
        }
    }

    private void logResponse(final HttpServletRequest httpRequest, final TeeHttpServletResponse response) throws IOException {
        if (isNormalResponseOrLastResponseOfAsyncDispatch(httpRequest)) {
            writer.writeResponse(formatter.format(response));
        }
    }

    private boolean isNormalResponseOrLastResponseOfAsyncDispatch(final HttpServletRequest request) {
        return !request.isAsyncStarted();
    }

}
