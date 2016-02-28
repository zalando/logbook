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
import org.mockito.ArgumentCaptor;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies that {@link LogbookFilter} delegates to {@link HttpLogWriter} correctly.
 */
public final class WritingTest {

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
    public void shouldLogRequest() throws Exception {
        given()
                .when()
                .contentType(ContentType.JSON)
                .content("{\"value\":\"Hello, world!\"}")
                .post(server.url("/echo?mode=byte"));

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<Precorrelation<String>> captor = ArgumentCaptor.forClass(Precorrelation.class);
        verify(writer).writeRequest(captor.capture());
        final Precorrelation<String> precorrelation = captor.getValue();
        final String request = precorrelation.getRequest();

        assertThat(request, startsWith("Request:"));
        assertThat(request, containsString("POST http://localhost"));
        assertThat(request, containsString("/echo?mode=byte"));
        assertThat(request, containsString("Hello, world!"));
    }

    @Test
    public void shouldLogResponse() throws Exception {
        given()
                .when()
                .contentType(ContentType.JSON)
                .content("{\"value\":\"Hello, world!\"}")
                .post(server.url("/echo?mode=byte"));

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<Correlation<String, String>> captor = ArgumentCaptor.forClass(Correlation.class);
        verify(writer).writeResponse(captor.capture());
        final Correlation<String, String> correlation = captor.getValue();

        assertThat(correlation.getResponse(), startsWith("Response:"));
        assertThat(correlation.getResponse(), containsString("HTTP/1.1 200"));
        assertThat(correlation.getResponse(), containsString("Content-Type: application/json"));
        assertThat(correlation.getResponse(), containsString("Hello, world!"));
    }

}
