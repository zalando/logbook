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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.lang.String.format;

interface HttpFilter extends Filter {

    @Override
    default void init(final FilterConfig filterConfig) throws ServletException {
        
    }

    @Override
    default void doFilter(final ServletRequest request, final ServletResponse response,
            final FilterChain chain) throws ServletException, IOException {

        if (!(request instanceof HttpServletRequest)) {
          throw new IllegalArgumentException(format("%s only supports HTTP", getClass().getSimpleName()));
        }

        if (!(response instanceof HttpServletResponse)) {
          throw new IllegalArgumentException(format("%s only supports HTTP", getClass().getSimpleName()));
        }

        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        doFilter(httpRequest, httpResponse, chain);
    }
    
    void doFilter(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
                final FilterChain chain) throws ServletException, IOException;
    
    @Override
    default void destroy() {
        
    }

}
