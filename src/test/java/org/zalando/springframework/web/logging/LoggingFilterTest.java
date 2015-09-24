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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@WebAppConfiguration
public final class LoggingFilterTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private HttpLogger logger;

    @Before
    public void setUp() {
        reset(logger);

        when(logger.shouldLog(any(), any())).thenReturn(true);
    }

    @Test
    public void shouldLogRequest() throws Exception {
        mvc.perform(get("/api/sync"));

        final RequestData request = interceptRequest();

        assertThat(request, hasFeature("remote address", RequestData::getRemote, is("127.0.0.1")));
        assertThat(request, hasFeature("method", RequestData::getMethod, is("GET")));
        assertThat(request, hasFeature("url", RequestData::getUrl, is("http://localhost/api/sync")));
        assertThat(request, hasFeature("headers", RequestData::getHeaders, is(emptyMap())));
        assertThat(request, hasFeature("parameters", RequestData::getParameters, is(emptyMap())));
        assertThat(request, hasFeature("body", RequestData::getBody, is(emptyOrNullString())));
    }

    @Test
    public void shouldLogResponse() throws Exception {
        mvc.perform(get("/api/sync"));

        final ResponseData response = interceptResponse();

        assertThat(response, hasFeature("status", ResponseData::getStatus, is(200)));
        assertThat(response, hasFeature("headers", ResponseData::getHeaders,
                hasEntry("Content-Type", singletonList("application/json"))));
        assertThat(response, hasFeature("content type", ResponseData::getContentType, is("application/json")));

        with(response.getBody())
                .assertThat("$.*", hasSize(1))
                .assertThat("$.value", is("Hello, world!"));
    }

    @Test
    public void shouldLogAsyncRequest() throws Exception {
        mvc.perform(asyncDispatch(mvc.perform(get("/api/async"))
                .andExpect(request().asyncStarted())
                .andReturn()));

        final RequestData request = interceptRequest();

        assertThat(request, hasFeature("remote address", RequestData::getRemote, is("127.0.0.1")));
        assertThat(request, hasFeature("method", RequestData::getMethod, is("GET")));
        assertThat(request, hasFeature("url", RequestData::getUrl, is("http://localhost/api/async")));
        assertThat(request, hasFeature("headers", RequestData::getHeaders, is(emptyMap())));
        assertThat(request, hasFeature("parameters", RequestData::getParameters, is(emptyMap())));
        assertThat(request, hasFeature("body", RequestData::getBody, is(emptyOrNullString())));
    }

    @Test
    public void shouldLogAsyncResponse() throws Exception {
        mvc.perform(asyncDispatch(mvc.perform(get("/api/async"))
                .andExpect(request().asyncStarted())
                .andReturn()));

        final ResponseData response = interceptResponse();

        assertThat(response, hasFeature("status", ResponseData::getStatus, is(200)));
        assertThat(response, hasFeature("headers", ResponseData::getHeaders,
                hasEntry("Content-Type", singletonList("application/json"))));
        assertThat(response, hasFeature("content type", ResponseData::getContentType, is("application/json")));

        with(response.getBody())
                .assertThat("$.*", hasSize(1))
                .assertThat("$.value", is("Hello, world!"));
    }

    private RequestData interceptRequest() {
        final ArgumentCaptor<RequestData> captor = ArgumentCaptor.forClass(RequestData.class);
        verify(logger).logRequest(captor.capture());
        return captor.getValue();
    }

    private ResponseData interceptResponse() {
        final ArgumentCaptor<ResponseData> captor = ArgumentCaptor.forClass(ResponseData.class);
        verify(logger).logResponse(captor.capture());
        return captor.getValue();
    }

}
