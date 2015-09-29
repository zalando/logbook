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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class LogbookFilterUnitTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private final HttpLogFormatter formatter = mock(HttpLogFormatter.class);
    private final HttpLogWriter writer = mock(HttpLogWriter.class);
    private final LogbookFilter unit = new LogbookFilter(formatter, writer);

    private final ServletRequest request = mock(HttpServletRequest.class);
    private final ServletResponse response = mock(HttpServletResponse.class);
    private final FilterChain chain = mock(FilterChain.class);

    @Before
    public void defaultBehaviour() throws IOException {
        when(writer.isActive(any(), any())).thenReturn(true);
        when(request.getInputStream()).thenReturn(new ServletInputStream() {
            final InputStream stream = new ByteArrayInputStream("This is a test".getBytes(UTF_8));
            @Override
            public int read() throws IOException {
                return stream.read();
            }
        });
    }

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

    @Test
    @Ignore
    public void shouldAllowHttpRequestAndResponse() throws ServletException, IOException {
        unit.doFilter(request, response, chain);
    }

    @Test
    public void shouldBypassIfWriterIsInactive() throws ServletException, IOException {
        when(writer.isActive(any(), any())).thenReturn(false);

        unit.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    @Ignore
    public void shouldNotDelegateIfWriterIsActive() throws ServletException, IOException {
        unit.doFilter(request, response, chain);

        verify(chain, never()).doFilter(request, response);
    }

}