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

import org.springframework.core.Ordered;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/***
 * {@link OncePerRequestFilter} which allows to log the request and response including their payload. To do so, this
 * filter will consume and cache the request and response before passing it to the filter chain.
 * <p/>
 * This class is not marked as a {@link org.springframework.stereotype.Component} since you want to register it via a
 * org.springframework.boot.context.embedded.FilterRegistrationBean. Be sure to make use of the {@link Ordered}
 * interface when registering, since this filter should be run last to allow all other filters to modify the request
 * and response.
 */
public class LoggingFilter extends OncePerRequestFilter implements Ordered {

    private final HttpLogger httpLogger;

    private final LogDataBuilder dataBuilder;

    public LoggingFilter() {
        this(new DefaultHttpLogger(), new LogDataBuilder());
    }

    public LoggingFilter(final HttpLogger httpLogger) {
        this(httpLogger, new LogDataBuilder());
    }

    public LoggingFilter(final LogDataBuilder dataBuilder) {
        this(new DefaultHttpLogger(), dataBuilder);
    }

    public LoggingFilter(final HttpLogger httpLogger, final LogDataBuilder dataBuilder) {
        this.httpLogger = httpLogger;
        this.dataBuilder = dataBuilder;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain chain) throws IOException, ServletException {
        if (httpLogger.shouldLog(request, response)) {
            doLoggedFilterInternal(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }

    }

    protected void doLoggedFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain chain) throws IOException, ServletException {
        final ConsumingHttpServletRequestWrapper wrappedRequest = wrapRequest(request);
        final ContentCachingResponseWrapper wrappedResponse = wrapResponse(response);

        final boolean isFirstExecution = !isAsyncDispatch(request);
        final boolean isLastExecution = !isAsyncStarted(request);

        if (isFirstExecution) {
            logRequest(wrappedRequest);
        }
        try {
            chain.doFilter(wrappedRequest, wrappedResponse);
            writeResponse(wrappedResponse);
        } finally {
            if (isLastExecution) {
                logResponse(wrappedResponse);
            }
        }
    }

    private ConsumingHttpServletRequestWrapper wrapRequest(final HttpServletRequest request) {

        final ConsumingHttpServletRequestWrapper wrappedRequest;
        if (request instanceof ConsumingHttpServletRequestWrapper) {
            wrappedRequest = (ConsumingHttpServletRequestWrapper) request;
        } else {
            wrappedRequest = new ConsumingHttpServletRequestWrapper(new ContentCachingRequestWrapper(request));
        }
        return wrappedRequest;
    }

    private ContentCachingResponseWrapper wrapResponse(final HttpServletResponse response) {

        final ContentCachingResponseWrapper wrappedResponse;
        if (response instanceof ContentCachingResponseWrapper) {
            wrappedResponse = (ContentCachingResponseWrapper) response;
        } else {
            wrappedResponse = new ContentCachingResponseWrapper(response);
        }
        return wrappedResponse;
    }

    private void writeResponse(final ContentCachingResponseWrapper wrappedResponse) throws IOException {

        final byte[] body = wrappedResponse.getContentAsByteArray();
        final ServletResponse rawResponse = wrappedResponse.getResponse();
        if (body.length > 0) {
            if (!rawResponse.isCommitted()) {
                rawResponse.setContentLength(body.length);
            }
            StreamUtils.copy(body, rawResponse.getOutputStream());
        }
    }

    private void logRequest(final ConsumingHttpServletRequestWrapper request) {
        httpLogger.logRequest(dataBuilder.buildRequest(request));
    }

    private void logResponse(final ContentCachingResponseWrapper response) {
        httpLogger.logResponse(dataBuilder.buildResponse(response));
    }

}
