package org.zalando.logbook;

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

import com.google.common.collect.ImmutableMultimap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.zalando.logbook.Formatting.getHeaders;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@WebAppConfiguration
public final class LogbookFilterTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private HttpLogFormatter formatter;

    @Autowired
    private HttpLogWriter writer;

    @Before
    public void setUp() throws IOException {
        reset(formatter, writer);

        when(writer.isActive(any(), any())).thenReturn(true);
    }

    @Test
    public void shouldFormatRequest() throws Exception {
        mvc.perform(get("/api/sync"));

        final TeeHttpServletRequest request = interceptRequest();

        assertThat(request, hasFeature("remote address", HttpServletRequest::getRemoteAddr, is("127.0.0.1")));
        assertThat(request, hasFeature("method", HttpServletRequest::getMethod, is("GET")));
        assertThat(request, hasFeature("url", HttpServletRequest::getRequestURI, is("/api/sync")));
        assertThat(request, hasFeature("headers", Formatting::getHeaders, is(ImmutableMultimap.of())));
        assertThat(request, hasFeature("parameters", HttpServletRequest::getParameterMap, is(emptyMap())));
        assertThat(request, hasFeature("body", this::getBodyAsString, is(emptyOrNullString())));
    }

    @Test
    public void shouldLogRequest() throws Exception {
        mvc.perform(get("/api/sync")
                .accept(MediaType.APPLICATION_JSON)
                .header("Host", "localhost")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Hello, world!"));

        verify(writer).writeRequest("GET /api/sync HTTP/1.1\n" +
                "Accept: application/json\n" +
                "Host: localhost\n" +
                "Content-Type: text/plain\n" +
                "\n" +
                "Hello, world!");
    }

    @Test
    public void shouldFormatResponse() throws Exception {
        mvc.perform(get("/api/sync"));

        final TeeHttpServletResponse response = interceptResponse();

        assertThat(response, hasFeature("status", HttpServletResponse::getStatus, is(200)));
        assertThat(response, hasFeature("headers", r -> getHeaders(r).asMap(),
                hasEntry("Content-Type", singletonList("application/json"))));
        assertThat(response, hasFeature("content type", HttpServletResponse::getContentType, is("application/json")));

        with(response.getBodyAsString())
                .assertThat("$.*", hasSize(1))
                .assertThat("$.value", is("Hello, world!"));
    }

    @Test
    public void shouldLogResponse() throws Exception {
        mvc.perform(get("/api/sync"));

        verify(writer).writeResponse("HTTP/1.1 200\n" +
                "Content-Type: application/json\n" +
                "\n" +
                "{\"value\":\"Hello, world!\"}");
    }

    @Test
    public void shouldFormatAsyncRequest() throws Exception {
        mvc.perform(asyncDispatch(mvc.perform(get("/api/async"))
                .andExpect(request().asyncStarted())
                .andReturn()));

        final TeeHttpServletRequest request = interceptRequest();


        assertThat(request, hasFeature("remote address", HttpServletRequest::getRemoteAddr, is("127.0.0.1")));
        assertThat(request, hasFeature("method", HttpServletRequest::getMethod, is("GET")));
        assertThat(request, hasFeature("url", HttpServletRequest::getRequestURI, is("/api/async")));
        assertThat(request, hasFeature("headers", Formatting::getHeaders, is(ImmutableMultimap.of())));
        assertThat(request, hasFeature("parameters", HttpServletRequest::getParameterMap, is(emptyMap())));
        assertThat(request, hasFeature("body", this::getBodyAsString, is(emptyOrNullString())));
    }

    @Test
    public void shouldFormatAsyncResponse() throws Exception {
        mvc.perform(asyncDispatch(mvc.perform(get("/api/async"))
                .andExpect(request().asyncStarted())
                .andReturn()));

        final TeeHttpServletResponse response = interceptResponse();

        assertThat(response, hasFeature("status", HttpServletResponse::getStatus, is(200)));
        assertThat(response, hasFeature("headers", r -> getHeaders(r).asMap(),
                hasEntry("Content-Type", singletonList("application/json"))));
        assertThat(response, hasFeature("content type", HttpServletResponse::getContentType, is("application/json")));

        with(response.getBodyAsString())
                .assertThat("$.*", hasSize(1))
                .assertThat("$.value", is("Hello, world!"));
    }

    private String getBodyAsString(final ReadableBody readableBody) {
        try {
            return readableBody.getBodyAsString();
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    private TeeHttpServletRequest interceptRequest() throws IOException {
        final ArgumentCaptor<TeeHttpServletRequest> captor = ArgumentCaptor.forClass(TeeHttpServletRequest.class);
        verify(formatter).format(captor.capture());
        return captor.getValue();
    }

    private TeeHttpServletResponse interceptResponse() throws IOException {
        final ArgumentCaptor<TeeHttpServletResponse> captor = ArgumentCaptor.forClass(TeeHttpServletResponse.class);
        verify(formatter).format(captor.capture());
        return captor.getValue();
    }

}
