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
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.servlet.example.ExampleController;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Verifies that {@link LogbookFilter} handles cases correctly when multiple instances are running in the same chain.
 */
public final class MultiFilterTest {

    private final HttpLogFormatter formatter = spy(new ForwardingHttpLogFormatter(new DefaultHttpLogFormatter()));
    private final HttpLogWriter writer = mock(HttpLogWriter.class);

    private final Logbook logbook = Logbook.builder()
            .formatter(formatter)
            .writer(writer)
            .build();

    private final Filter firstFilter = spy(new SpyableFilter(new LogbookFilter(logbook)));
    private final Filter lastFilter = spy(new SpyableFilter(new LogbookFilter(logbook)));
    private final ExampleController controller = spy(new ExampleController());

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(controller)
            .addFilter(firstFilter)
            .addFilter(lastFilter)
            .build();

    @Before
    public void setUp() throws IOException {
        reset(formatter, writer);

        when(writer.isActive(any())).thenReturn(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldFormatRequestTwice() throws Exception {
        mvc.perform(get("/api/sync"));

        verify(formatter, times(2)).format(any(Precorrelation.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldFormatResponseTwice() throws Exception {
        mvc.perform(get("/api/sync"));

        verify(formatter, times(2)).format(any(Correlation.class));
    }

    @Test
    public void shouldLogRequestTwice() throws Exception {
        mvc.perform(get("/api/sync"));

        verify(writer, times(2)).writeRequest(any());
    }

    @Test
    public void shouldLogResponseTwice() throws Exception {
        mvc.perform(get("/api/sync"));

        verify(writer, times(2)).writeResponse(any());
    }

    @Test
    public void shouldBufferRequestTwice() throws Exception {
        mvc.perform(get("/api/read-byte")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Hello, world!")).andReturn();

        final TeeRequest firstRequest = getRequest(lastFilter);
        final TeeRequest secondRequest = getRequest(controller);

        assertThat(firstRequest.getOutput().toByteArray().length, is(greaterThan(0)));
        assertThat(secondRequest.getOutput().toByteArray().length, is(greaterThan(0)));
    }

    @Test
    public void shouldBufferResponseTwice() throws Exception {
        mvc.perform(get("/api/read-bytes")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Hello, world!")).andReturn();

        final TeeResponse firstResponse = getResponse(lastFilter);
        final TeeResponse secondResponse = getResponse(controller);

        assertThat(firstResponse.getOutput().toByteArray().length, is(greaterThan(0)));
        assertThat(secondResponse.getOutput().toByteArray().length, is(greaterThan(0)));
    }

    private TeeRequest getRequest(final Filter filter) throws IOException, ServletException {
        final ArgumentCaptor<TeeRequest> captor = ArgumentCaptor.forClass(TeeRequest.class);
        verify(filter).doFilter(captor.capture(), any(), any());
        return captor.getValue();
    }

    private TeeRequest getRequest(final ExampleController controller) throws IOException {
        final ArgumentCaptor<TeeRequest> captor = ArgumentCaptor.forClass(TeeRequest.class);
        verify(controller).readByte(captor.capture(), any());
        return captor.getValue();
    }

    private TeeResponse getResponse(final Filter filter) throws IOException, ServletException {
        final ArgumentCaptor<TeeResponse> captor = ArgumentCaptor.forClass(TeeResponse.class);
        verify(filter).doFilter(any(), captor.capture(), any());
        return captor.getValue();
    }

    private TeeResponse getResponse(final ExampleController controller) throws IOException {
        final ArgumentCaptor<TeeResponse> captor = ArgumentCaptor.forClass(TeeResponse.class);
        verify(controller).readBytes(any(), captor.capture());
        return captor.getValue();
    }

}
