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

import org.zalando.logbook.Correlator;
import org.zalando.logbook.Logbook;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static org.zalando.logbook.servlet.Attributes.CORRELATION;

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
        final Optional<Correlator> correlator = logRequest(request);

        if (correlator.isPresent()) {
            final TeeResponse response = new TeeResponse(httpRequest, httpResponse);

            try {
                chain.doFilter(request, response);
            } finally {
                logResponse(correlator, request, response);
            }
        } else {
            chain.doFilter(httpRequest, httpResponse);
        }
    }

    private Optional<Correlator> logRequest(final TeeRequest request) throws IOException {
        if (!isAsyncDispatch(request)) {
            final Optional<Correlator> correlator = logbook.write(request);
            writeCorrelator(request, correlator);
            return correlator;
        } else {
            return readCorrelator(request);
        }
    }

    private void writeCorrelator(final TeeRequest request, final Optional<Correlator> correlator) {
        request.setAttribute(CORRELATION, correlator.orElse(null));
    }

    private Optional<Correlator> readCorrelator(final TeeRequest request) {
        return Optional.ofNullable(request.getAttribute(CORRELATION)).map(Correlator.class::cast);
    }

    private void logResponse(final Optional<Correlator> correlator, final TeeRequest request,
            final TeeResponse response) throws IOException {

        if (!request.isAsyncStarted() && correlator.isPresent()) {
            correlator.get().write(response);
            response.close();
        }
    }

}
