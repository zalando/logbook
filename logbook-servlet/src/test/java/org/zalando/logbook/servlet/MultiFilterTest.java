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
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.JsonHttpLogFormatter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import java.io.IOException;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies that {@link LogbookFilter} handles cases correctly when multiple instances are running in the same chain.
 */
public final class MultiFilterTest {

    private final HttpLogFormatter formatter = spy(new ForwardingHttpLogFormatter(new JsonHttpLogFormatter()));
    private final HttpLogWriter writer = mock(HttpLogWriter.class);

    private final Logbook logbook = Logbook.builder()
            .formatter(formatter)
            .writer(writer)
            .build();

    private final Filter firstFilter = spy(new SpyableFilter(new LogbookFilter(logbook)));
    private final Filter lastFilter = spy(new SpyableFilter(new LogbookFilter(logbook)));
    private final SpyableFilter spy = spy(new SpyableFilter());

    @Rule
    public final ServerRule server = new ServerRule(firstFilter, lastFilter, spy);

    @Before
    public void setUp() throws IOException {
        when(writer.isActive(any())).thenReturn(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldFormatRequestTwice() throws IOException {
        given().when()
                .post(server.url("/echo"));

        verify(formatter, times(2)).format(any(Precorrelation.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldFormatResponseTwice() throws IOException {
        given().when()
                .post(server.url("/echo"));

        verify(formatter, times(2)).format(any(Correlation.class));
    }

    @Test
    public void shouldLogRequestTwice() throws IOException {
        given().when()
                .post(server.url("/echo"));

        verify(writer, times(2)).writeRequest(any());
    }

    @Test
    public void shouldLogResponseTwice() throws IOException {
        given().when()
                .post(server.url("/echo"));

        verify(writer, times(2)).writeResponse(any());
    }

    @Test
    public void shouldBufferRequestTwice() throws IOException, ServletException {
        given().when()
                .content("Hello, world!")
                .post(server.url("/echo?mode=byte"));

        final TeeRequest firstRequest = getRequest(lastFilter);
        final TeeRequest secondRequest = getRequest(spy);

        assertThat(firstRequest.getOutput().toByteArray().length, is(greaterThan(0)));
        assertThat(secondRequest.getOutput().toByteArray().length, is(greaterThan(0)));
    }

    @Test
    public void shouldBufferResponseTwice() throws IOException, ServletException {
        given().when()
                .content("Hello, world!")
                .post(server.url("/echo?mode=bytes"));

        final TeeResponse firstResponse = getResponse(lastFilter);
        final TeeResponse secondResponse = getResponse(spy);

        assertThat(firstResponse.getOutput().toByteArray().length, is(greaterThan(0)));
        assertThat(secondResponse.getOutput().toByteArray().length, is(greaterThan(0)));
    }

    private TeeRequest getRequest(final Filter filter) throws IOException, ServletException {
        final ArgumentCaptor<TeeRequest> captor = ArgumentCaptor.forClass(TeeRequest.class);
        verify(filter).doFilter(captor.capture(), any(), any());
        return captor.getValue();
    }

    private TeeResponse getResponse(final Filter filter) throws IOException, ServletException {
        final ArgumentCaptor<TeeResponse> captor = ArgumentCaptor.forClass(TeeResponse.class);
        verify(filter).doFilter(any(), captor.capture(), any());
        return captor.getValue();
    }

}
