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

import com.google.common.collect.Lists;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.servlet.example.ExampleController;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singleton;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@Configuration
@EnableWebMvc
@Import(ExampleController.class)
public class MultiFilterTestConfiguration {

    @Bean
    public MockMvc mockMvc(final WebApplicationContext context,
            @First final Filter firstFilter, @Second final Filter secondFilter) {
        return MockMvcBuilders.webAppContextSetup(context)
                .addFilter(firstFilter)
                .addFilter(secondFilter)
                .build();
    }

    @Bean
    @First
    public Filter firstFilter(@First final Logbook logbook) throws ServletException, IOException {
        return spyOn(new LogbookFilter(logbook));
    }

    @Bean
    @First
    public Logbook firstLogbook(@First final HttpLogFormatter formatter, @First final HttpLogWriter writer) {
        return Logbook.builder()
                .formatter(formatter)
                .writer(writer)
                .build();
    }

    @Bean
    @First
    public HttpLogFormatter firstHttpLogFormatter() {
        // otherwise we would need to make DefaultHttpLogFormatter non-final
        return spy(new ForwardingHttpLogFormatter(new DefaultHttpLogFormatter()));
    }

    @Bean
    @First
    public HttpLogWriter firstHttpLogWriter() {
        return mock(HttpLogWriter.class);
    }

    @Bean
    @Second
    public Filter secondFilter(@Second final Logbook logbook) throws IOException, ServletException {
        return spyOn(new LogbookFilter(logbook));
    }

    @Bean
    @Second
    public Logbook secondLogbook(@Second final HttpLogFormatter formatter, @Second final HttpLogWriter writer) {
        return Logbook.builder()
                .formatter(formatter)
                .writer(writer)
                .build();
    }

    @Bean
    @Second
    public HttpLogFormatter secondHttpLogFormatter() {
        // otherwise we would need to make DefaultHttpLogFormatter non-final
        return spy(new ForwardingHttpLogFormatter(new DefaultHttpLogFormatter()));
    }

    @Bean
    @Second
    public HttpLogWriter secondHttpLogWriter() {
        return mock(HttpLogWriter.class);
    }

    private OnceFilter spyOn(final LogbookFilter filter) throws IOException, ServletException {
        // otherwise we would need to make LogbookFilter non-final
        final OnceFilter spyFilter = spy(new ForwardingOnceFilter(filter));

        final Answer spyOnChainDelegation = invocation -> {
            final Object[] arguments = invocation.getArguments();

            final FilterChain chain = (FilterChain) arguments[2];
            final FilterChain spyChain = spy(chain);

            doAnswer(interceptTeeRequestsAndResponses(chain)).when(spyChain).doFilter(any(), any());

            final HttpServletRequest request = (HttpServletRequest) arguments[0];
            final HttpServletResponse response = (HttpServletResponse) arguments[1];
            filter.doFilter(request, response, spyChain);

            return null;
        };

        doAnswer(spyOnChainDelegation).when(spyFilter)
                .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class), any());

        return spyFilter;
    }

    private Answer interceptTeeRequestsAndResponses(final FilterChain chain) {
        return invocation -> {
            final Object[] arguments = invocation.getArguments();
            final TeeRequest request = (TeeRequest) arguments[0];
            final TeeResponse response = (TeeResponse) arguments[1];

            appendTo(request, TestAttributes.REQUESTS, request);
            appendTo(request, TestAttributes.RESPONSES, response);

            chain.doFilter(request, response);

            return null;
        };
    }

    private <T> void appendTo(final ServletRequest request, final String attributeName, final T element) {
        @SuppressWarnings("unchecked")
        final List<T> list = (List<T>) request.getAttribute(attributeName);

        if (list == null) {
            request.setAttribute(attributeName, new ArrayList<>(singleton(element)));
        } else {
            list.add(element);
        }
    }

}
