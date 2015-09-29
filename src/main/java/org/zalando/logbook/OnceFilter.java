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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkArgument;

abstract class OnceFilter implements Filter, DispatchAware, Markable, Named, Skippable {

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // nothing to do
    }

    @Override
    public final void doFilter(final ServletRequest request, final ServletResponse response,
            final FilterChain chain) throws ServletException, IOException {

        checkArgument(request instanceof HttpServletRequest, "%s only supports HTTP", getClass().getSimpleName());
        checkArgument(response instanceof HttpServletResponse, "%s only supports HTTP", getClass().getSimpleName());

        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        doFilterIfNecessary(httpRequest, httpResponse, chain);
    }

    private void doFilterIfNecessary(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain chain) throws ServletException, IOException {
        if (skipDispatch(request)) {
            chain.doFilter(request, response);
        } else {
            filter(request, response, chain);
        }
    }

    private void filter(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain filterChain) throws ServletException, IOException {

        mark(request);
        try {
            doFilter(request, response, filterChain);
        } finally {
            unmark(request);
        }
    }

    private boolean skipDispatch(final HttpServletRequest request) {
        return isMarked(request) ||
                skip(request) ||
                isAsyncDispatch(request) && skipAsyncDispatch() ||
                isErrorDispatch(request) && skipErrorDispatch();
    }

    protected abstract void doFilter(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain chain) throws ServletException, IOException;

    @Override
    public void destroy() {
        // nothing to do
    }

}
