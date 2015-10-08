package org.zalando.logbook.servlet;

/*
 * #%L
 * logbook
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

import com.google.common.collect.ImmutableMultimap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.HttpMessage;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;

import javax.servlet.DispatcherType;
import java.io.IOException;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

/**
 * Verifies that {@link LogbookFilter} handles {@link DispatcherType#ASYNC} correctly.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@WebAppConfiguration
public final class AsyncDispatchTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private HttpLogFormatter formatter;

    @Autowired
    private HttpLogWriter writer;

    @Before
    public void setUp() throws IOException {
        reset(formatter, writer);

        when(writer.isActive(any())).thenReturn(true);
    }

    @Test
    public void shouldFormatAsyncRequest() throws Exception {
        mvc.perform(async(mvc.perform(get("/api/async"))
                .andExpect(request().asyncStarted())
                .andReturn()));

        final HttpRequest request = interceptRequest();

        assertThat(request, hasFeature("remote address", HttpRequest::getRemote, is("127.0.0.1")));
        assertThat(request, hasFeature("method", HttpRequest::getMethod, is("GET")));
        assertThat(request, hasFeature("url", HttpRequest::getRequestUri, hasToString("/api/async")));
        assertThat(request, hasFeature("headers", HttpRequest::getHeaders, is(ImmutableMultimap.of())));
        assertThat(request, hasFeature("body", this::getBodyAsString, is(emptyOrNullString())));
    }

    @Test
    public void shouldFormatAsyncResponse() throws Exception {
        mvc.perform(async(mvc.perform(get("/api/async"))
                .andExpect(request().asyncStarted())
                .andReturn()));

        final HttpResponse response = interceptResponse();

        assertThat(response, hasFeature("status", HttpResponse::getStatus, is(200)));
        assertThat(response, hasFeature("headers", r -> r.getHeaders().asMap(),
                hasEntry("Content-Type", singletonList("application/json"))));
        assertThat(response, hasFeature("content type", HttpResponse::getContentType, is("application/json")));

        with(response.getBodyAsString())
                .assertThat("$.*", hasSize(1))
                .assertThat("$.value", is("Hello, world!"));
    }

    private RequestBuilder async(final MvcResult result) throws Exception {
        result.getAsyncResult();

        return context -> {
            final MockHttpServletRequest request = result.getRequest();
            // this was missing in the asyncDispatch builder from Spring
            request.setDispatcherType(DispatcherType.ASYNC);
            request.setAsyncStarted(false);
            return request;
        };
    }

    private String getBodyAsString(final HttpMessage message) {
        try {
            return message.getBodyAsString();
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    private HttpRequest interceptRequest() throws IOException {
        @SuppressWarnings("unchecked")
        final ArgumentCaptor<Precorrelation<HttpRequest>> captor = ArgumentCaptor.forClass(Precorrelation.class);
        verify(formatter).format(captor.capture());
        return captor.getValue().getRequest();
    }

    private HttpResponse interceptResponse() throws IOException {
        @SuppressWarnings("unchecked")
        final ArgumentCaptor<Correlation<HttpRequest, HttpResponse>> captor = ArgumentCaptor.forClass(Correlation.class);
        verify(formatter).format(captor.capture());
        return captor.getValue().getResponse();
    }

}
