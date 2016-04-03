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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.zalando.logbook.Logbook;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.mock;

/**
 * Verifies that {@link LogbookFilter} rejects non-HTTP requests/responses.
 */
public final class HttpSupportTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private final Logbook logbook = mock(Logbook.class);
    private final LogbookFilter unit = new LogbookFilter(logbook);

    private final ServletRequest request = mock(HttpServletRequest.class);
    private final ServletResponse response = mock(HttpServletResponse.class);
    private final FilterChain chain = mock(FilterChain.class);

    @Test
    public void shouldRejectNonHttpRequest() throws ServletException, IOException {
        final ServletRequest nonHttpRequest = mock(ServletRequest.class);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("LogbookFilter only supports HTTP");

        unit.doFilter(nonHttpRequest, response, chain);
    }

    @Test
    public void shouldRejectNonHttpResponse() throws ServletException, IOException {
        final ServletResponse nonHttpResponse = mock(ServletResponse.class);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("LogbookFilter only supports HTTP");

        unit.doFilter(request, nonHttpResponse, chain);
    }

}
