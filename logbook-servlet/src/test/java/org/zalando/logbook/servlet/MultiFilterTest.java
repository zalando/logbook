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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Precorrelation;

import javax.servlet.ServletRequest;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Verifies that {@link LogbookFilter} handles cases correctly when multiple instances are running in the same chain.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MultiFilterTestConfiguration.class)
@WebAppConfiguration
public final class MultiFilterTest {

    private final String uri = "/api/sync";

    @Autowired
    private MockMvc mvc;

    @Autowired
    @First
    private HttpLogFormatter firstFormatter;

    @Autowired
    @First
    private HttpLogWriter firstWriter;

    @Autowired
    @Second
    private HttpLogFormatter secondFormatter;

    @Autowired
    @Second
    private HttpLogWriter secondWriter;

    @Before
    public void setUp() throws IOException {
        reset(firstFormatter, secondFormatter, firstWriter, secondWriter);

        when(firstWriter.isActive(any())).thenReturn(true);
        when(secondWriter.isActive(any())).thenReturn(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldFormatRequestTwice() throws Exception {
        mvc.perform(get(uri));

        verify(firstFormatter).format(any(Precorrelation.class));
        verify(secondFormatter).format(any(Precorrelation.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldFormatResponseTwice() throws Exception {
        mvc.perform(get(uri));

        verify(firstFormatter).format(any(Correlation.class));
        verify(secondFormatter).format(any(Correlation.class));
    }

    @Test
    public void shouldLogRequestTwice() throws Exception {
        mvc.perform(get(uri));

        verify(firstWriter).writeRequest(any());
        verify(secondWriter).writeRequest(any());
    }

    @Test
    public void shouldLogResponseTwice() throws Exception {
        mvc.perform(get(uri));

        verify(firstWriter).writeResponse(any());
        verify(secondWriter).writeResponse(any());
    }

    @Test
    public void shouldBufferRequestOnlyOnce() throws Exception {
        final MvcResult result = mvc.perform(get("/api/read-byte")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Hello, world!")).andReturn();

        final MockHttpServletRequest request = result.getRequest();

        final List<TeeRequest> teeRequests = getList(request, TestAttributes.REQUESTS);

        assertThat(teeRequests, is(notNullValue()));
        assertThat(teeRequests, hasSize(2));

        final TeeRequest firstRequest = teeRequests.get(0);
        final TeeRequest secondRequest = teeRequests.get(1);
        
        assertThat(firstRequest.getOutput().toByteArray().length, is(greaterThan(0)));
        assertThat(secondRequest.getOutput().toByteArray().length, is(equalTo(0)));
    }

    @Test
    public void shouldBufferResponseOnlyOnce() throws Exception {
        final MvcResult result = mvc.perform(get("/api/read-bytes")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Hello, world!")).andReturn();

        final MockHttpServletRequest request = result.getRequest();

        final List<TeeResponse> teeResponses = getList(request, TestAttributes.RESPONSES);
        assertThat(teeResponses, hasSize(2));

        final TeeResponse firstResponse = teeResponses.get(0);
        final TeeResponse secondResponse = teeResponses.get(1);

        assertThat(firstResponse.getOutput().toByteArray().length, is(equalTo(0)));
        assertThat(secondResponse.getOutput().toByteArray().length, is(greaterThan(0)));
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> getList(final ServletRequest request, final String attributeName) {
        return (List<T>) request.getAttribute(attributeName);
    }

}
