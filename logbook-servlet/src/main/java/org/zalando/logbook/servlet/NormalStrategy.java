package org.zalando.logbook.servlet;

/*
 * #%L
 * Logbook: Servlet
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
import java.util.function.Consumer;

import static org.zalando.logbook.servlet.Attributes.CORRELATOR;

final class NormalStrategy implements Strategy {

    @Override
    public void doFilter(final Logbook logbook, final HttpServletRequest httpRequest,
            final HttpServletResponse httpResponse, final FilterChain chain) throws ServletException, IOException {

        final RemoteRequest request = new RemoteRequest(httpRequest);
        final Optional<Correlator> correlator = logRequestIfNecessary(logbook, request);

        if (correlator.isPresent()) {
            final LocalResponse response = new LocalResponse(httpResponse);

            chain.doFilter(request, response);
            response.getWriter().flush();
            logResponse(correlator.get(), request, response);
        } else {
            chain.doFilter(httpRequest, httpResponse);
        }
    }

    private Optional<Correlator> logRequestIfNecessary(final Logbook logbook, final RemoteRequest request) throws IOException {
        if (isFirstRequest(request)) {
            final Optional<Correlator> correlator = logbook.write(request);
            correlator.ifPresent(writeCorrelator(request));
            return correlator;
        } else {
            return readCorrelator(request);
        }
    }

    private Consumer<Correlator> writeCorrelator(final RemoteRequest request) {
        return correlator -> request.setAttribute(CORRELATOR, correlator);
    }

    private Optional<Correlator> readCorrelator(final RemoteRequest request) {
        return Optional.ofNullable(request.getAttribute(CORRELATOR)).map(Correlator.class::cast);
    }

    private void logResponse(final Correlator correlator, final RemoteRequest request,
            final LocalResponse response) throws IOException {

        if (isLastRequest(request)) {
            correlator.write(response);
        }
    }

}
