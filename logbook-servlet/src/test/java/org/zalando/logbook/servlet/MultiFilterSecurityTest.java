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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies that {@link LogbookFilter} handles complex security setups correctly.
 */
public final class MultiFilterSecurityTest {

    private final HttpLogFormatter formatter = spy(new ForwardingHttpLogFormatter(new JsonHttpLogFormatter()));
    private final HttpLogWriter writer = mock(HttpLogWriter.class);

    private final Logbook logbook = Logbook.builder()
            .formatter(formatter)
            .writer(writer)
            .build();

    private final Filter firstFilter = spy(new SpyableFilter(new LogbookFilter(logbook, Strategy.SECURITY)));
    private final SecurityFilter securityFilter = spy(new SecurityFilter());
    private final Filter lastFilter = spy(new SpyableFilter(new LogbookFilter(logbook)));
    private final SpyableFilter spy = spy(new SpyableFilter());

    @Rule
    public final ServerRule server = new ServerRule(firstFilter, securityFilter, lastFilter, spy);

    @Before
    public void setUp() throws IOException {
        when(writer.isActive(any())).thenReturn(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldFormatAuthorizedRequestOnce() throws IOException {
        given().when().post(server.url("/echo"));

        verify(formatter).format(any(Precorrelation.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldFormatAuthorizedResponseOnce() throws IOException {
        given().when().post(server.url("/echo"));

        verify(formatter).format(any(Correlation.class));
    }

    @Test
    public void shouldLogAuthorizedRequestOnce() throws IOException {
        given().when().post(server.url("/echo"));

        verify(writer).writeRequest(any());
    }

    @Test
    public void shouldLogAuthorizedResponseOnce() throws IOException {
        given().when().post(server.url("/echo"));

        verify(writer).writeResponse(any());
    }

    @Test
    public void shouldBufferAuthorizedRequestOnlyOnce() throws IOException, ServletException {
        given().when()
                .content("Hello, world!")
                .post(server.url("/echo?mode=byte"));

        final TeeRequest firstRequest = getRequest(securityFilter);
        final TeeRequest secondRequest = getRequest(spy);

        assertThat(firstRequest.getOutput().toByteArray().length, is(equalTo(0)));
        assertThat(secondRequest.getOutput().toByteArray().length, is(greaterThan(0)));
    }

    @Test
    public void shouldBufferAuthorizedResponseTwice() throws IOException, ServletException {
        given().when()
                .content("Hello, world!")
                .post(server.url("/echo?mode=bytes"));

        final TeeResponse firstResponse = getResponse(securityFilter);
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

    @Test
    @SuppressWarnings("unchecked")
    public void shouldFormatUnauthorizedRequestOnce() throws IOException {
        securityFilter.setStatus(401);

        given().when().post(server.url("/echo"));

        verify(formatter).format(any(Precorrelation.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldFormatUnauthorizedResponseOnce() throws IOException {
        securityFilter.setStatus(401);

        given().when().post(server.url("/echo"));

        verify(formatter).format(any(Correlation.class));
    }

    @Test
    public void shouldLogUnauthorizedRequestOnce() throws IOException {
        securityFilter.setStatus(401);

        given().when().post(server.url("/echo"));

        verify(writer).writeRequest(any());
    }

    @Test
    public void shouldLogUnauthorizedResponseOnce() throws IOException {
        securityFilter.setStatus(401);

        given().when().post(server.url("/echo"));

        verify(writer).writeResponse(any());
    }

    @Test
    public void shouldNotLogRequestBodyForUnauthorizedRequests() throws IOException {
        securityFilter.setStatus(401);

        given().when()
                .content("Hello, world!")
                .post(server.url("/echo"));

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<Precorrelation<String>> captor = ArgumentCaptor.forClass(Precorrelation.class);
        verify(writer).writeRequest(captor.capture());
        final Precorrelation<String> precorrelation = captor.getValue();

        assertThat(precorrelation.getRequest(), not(containsString("Hello, world")));
    }

    @Test
    public void shouldNotLogUnauthorizedRequest() throws IOException {
        when(writer.isActive(any())).thenReturn(false);
        securityFilter.setStatus(401);

        given().when().post(server.url("/echo"));

        verify(writer, never()).writeRequest(any());
        verify(writer, never()).writeResponse(any());
    }

    @Test
    public void shouldHandleUnauthorizedAsyncDispatchRequest() {
        given().when().post(server.url("/unauthorized"));
    }

}
