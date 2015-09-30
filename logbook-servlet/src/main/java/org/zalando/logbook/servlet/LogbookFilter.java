package org.zalando.logbook.servlet;

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

import org.zalando.logbook.Logbook;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static org.zalando.logbook.servlet.Attributes.CORRELATION_ID;

public final class LogbookFilter extends OnceFilter implements DispatchAware {


    private final Logbook logbook;

    public LogbookFilter() {
        this(Logbook.create());
    }

    public LogbookFilter(final Logbook logbook) {
        this.logbook = logbook;
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
    protected void doFilter(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
            final FilterChain chain) throws ServletException, IOException {

        final TeeRequest request = new TeeRequest(httpRequest);
        final Optional<String> correlationId = logRequest(request);

        if (correlationId.isPresent()) {
            final TeeResponse response = new TeeResponse(httpRequest, httpResponse);

            try {
                chain.doFilter(request, response);
            } finally {
                logResponse(request, response, correlationId);
            }
        } else {
            chain.doFilter(httpRequest, httpResponse);
        }
    }

    private Optional<String> logRequest(final TeeRequest request) throws IOException {
        if (!isAsyncDispatch(request)) {
            final Optional<String> correlationId = logbook.write(request);
            writeCorrelationId(request, correlationId);
            return correlationId;
        } else {
            return readCorrelationId(request);
        }
    }

    private void writeCorrelationId(final TeeRequest request, final Optional<String> correlationId) {
        request.setAttribute(CORRELATION_ID, correlationId.orElse(null));
    }

    private Optional<String> readCorrelationId(final TeeRequest request) {
        return Optional.ofNullable(request.getAttribute(CORRELATION_ID)).map(String.class::cast);
    }

    private void logResponse(final TeeRequest request, final TeeResponse response,
            final Optional<String> correlationId) throws IOException {

        if (!request.isAsyncStarted()) {
            logbook.write(response, correlationId.get());
        }
    }

}
