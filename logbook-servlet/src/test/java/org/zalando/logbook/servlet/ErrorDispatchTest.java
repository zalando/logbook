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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.logbook.*;
import org.zalando.logbook.servlet.example.ExampleController;

import javax.servlet.DispatcherType;
import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Verifies that {@link LogbookFilter} handles {@link DispatcherType#ERROR} correctly.
 */
public final class ErrorDispatchTest {

    private final HttpLogFormatter formatter = spy(new ForwardingHttpLogFormatter(new DefaultHttpLogFormatter()));
    private final HttpLogWriter writer = mock(HttpLogWriter.class);

    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new ExampleController())
            .addFilter(new LogbookFilter(Logbook.builder()
                    .formatter(formatter)
                    .writer(writer)
                    .build()))
            .build();

    @Before
    public void setUp() throws IOException {
        reset(formatter, writer);

        when(writer.isActive(any())).thenReturn(true);
    }

    // TODO this is not actually producing an error dispatch
    @Test
    public void shouldFormatErrorResponse() throws Exception {
        mvc.perform(get("/api/not-found"));

        final HttpResponse response = interceptResponse();

        assertThat(response, hasFeature("status", HttpResponse::getStatus, is(404)));
        assertThat(response, hasFeature("headers", HttpResponse::getHeaders, is(Multimaps.immutableOf())));
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
