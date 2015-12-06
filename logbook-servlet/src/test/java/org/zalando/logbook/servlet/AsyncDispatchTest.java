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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;

import javax.servlet.DispatcherType;
import java.io.IOException;

import static com.jayway.jsonassert.JsonAssert.with;
import static com.jayway.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.servlet.Helper.anyCorrelation;
import static org.zalando.logbook.servlet.Helper.anyPrecorrelation;
import static org.zalando.logbook.servlet.Helper.validateRequest;
import static org.zalando.logbook.servlet.Helper.validateResponse;

/**
 * Verifies that {@link LogbookFilter} handles {@link DispatcherType#ASYNC} correctly.
 */
public final class AsyncDispatchTest {

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
    }

    @Test
    public void shouldFormatAsyncRequest() throws Exception {
        doAnswer(validateRequest(request -> {
            assertThat(request, hasFeature("remote address", HttpRequest::getRemote, is("127.0.0.1")));
            assertThat(request, hasFeature("method", HttpRequest::getMethod, is("POST")));
            assertThat(request, hasFeature("url", HttpRequest::getRequestUri,
                    hasToString(allOf(startsWith("http://localhost"), endsWith("/echo?async=true")))));
            assertThat(request, hasFeature("body", Helper::getBodyAsString, is(emptyOrNullString())));
        })).when(formatter).format(anyPrecorrelation());

        given().when().post(server.url("/echo?async=true"));
    }

    @Test
    public void shouldFormatAsyncResponse() throws Exception {
        doAnswer(validateResponse(response -> {
            assertThat(response, hasFeature("status", HttpResponse::getStatus, is(200)));
            assertThat(response, hasFeature("headers", r -> r.getHeaders().asMap(),
                    hasEntry("Content-Type", singletonList("application/json"))));
            assertThat(response, hasFeature("content type", HttpResponse::getContentType, is("application/json")));

            with(response.getBodyAsString())
                    .assertThat("$.*", hasSize(1))
                    .assertThat("$.value", is("Hello, world!"));
        })).when(formatter).format(anyCorrelation());

        given().when().post(server.url("/echo?async=true"));
    }

}
