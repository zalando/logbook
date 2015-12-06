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

import com.jayway.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.HttpMessage;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;

import java.io.IOException;

import static com.jayway.jsonassert.JsonAssert.with;
import static com.jayway.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.servlet.Helper.anyCorrelation;
import static org.zalando.logbook.servlet.Helper.anyPrecorrelation;
import static org.zalando.logbook.servlet.Helper.validateRequest;
import static org.zalando.logbook.servlet.Helper.validateResponse;

/**
 * Verifies that {@link LogbookFilter} delegates to {@link HttpLogFormatter} correctly.
 */
public final class FormattingTest {

    private final HttpLogFormatter formatter = spy(new ForwardingHttpLogFormatter(new DefaultHttpLogFormatter()));
    private final HttpLogWriter writer = mock(HttpLogWriter.class);

    @Rule
    public final ServerRule server = new ServerRule(new LogbookFilter(Logbook.builder()
            .formatter(formatter)
            .writer(writer)
            .build()));

    @Before
    public void setUp() throws IOException {
        when(writer.isActive(any())).thenReturn(true);
        doThrow(new UnsupportedOperationException()).when(formatter).format(anyPrecorrelation());
        doThrow(new UnsupportedOperationException()).when(formatter).format(anyCorrelation());
    }

    @Test
    public void shouldFormatRequest() throws Exception {
        doAnswer(validateRequest(request -> {
            assertThat(request, hasFeature("remote address", HttpRequest::getRemote, is("127.0.0.1")));
            assertThat(request, hasFeature("method", HttpRequest::getMethod, is("POST")));
            assertThat(request, hasFeature("url", HttpRequest::getRequestUri,
                    hasToString("http://localhost/echo?limit=1")));
            assertThat(request, hasFeature("headers", HttpRequest::getHeaders, is(singletonMap("Accept", "text/plain"))));
            assertThat(request, hasFeature("body", this::getBody, is(notNullValue())));
            assertThat(request, hasFeature("body", Helper::getBodyAsString, is(emptyString())));
        })).when(formatter).format(anyPrecorrelation());

        given().when()
                .contentType(ContentType.JSON)
                .content("{\"value\":\"Hello, world!\"}")
                .post(server.url("/echo?limit=1"));
    }

    @Test
    public void shouldFormatResponse() throws Exception {
        doAnswer(validateResponse(response -> {
            assertThat(response, hasFeature("status", HttpResponse::getStatus, is(200)));
            assertThat(response, hasFeature("headers", r -> r.getHeaders().asMap(),
                    hasEntry("Content-Type", singletonList(containsString("application/json")))));
            assertThat(response, hasFeature("content type", HttpResponse::getContentType, is("application/json")));

            with(response.getBodyAsString())
                    .assertThat("$.*", hasSize(1))
                    .assertThat("$.value", is("Hello, world!"));
        })).when(formatter).format(anyCorrelation());

        given().when()
                .contentType(ContentType.JSON)
                .content("{\"value\":\"Hello, world!\"}")
                .post(server.url("/echo"));
    }

    @Test
    public void shouldFormatResponseWithoutBody() throws Exception {
        doAnswer(validateResponse(response -> {
            assertThat(response, hasFeature("status", HttpResponse::getStatus, is(200)));
            assertThat(response, hasFeature("body", this::getBody, is(notNullValue())));
            assertThat(response, hasFeature("body", Helper::getBodyAsString, is(emptyString())));
        })).when(formatter).format(anyCorrelation());

        given().when()
                .post(server.url("/echo"));
    }

    private byte[] getBody(final HttpMessage message) {
        try {
            return message.getBody();
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

}
