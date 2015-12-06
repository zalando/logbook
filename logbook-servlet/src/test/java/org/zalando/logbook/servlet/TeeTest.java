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
import org.zalando.logbook.Logbook;

import java.io.IOException;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Verifies that {@link LogbookFilter} handles the copying of streams in {@link TeeRequest} and {@link TeeResponse}
 * correctly.
 */
public final class TeeTest {

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
    public void shouldWriteResponse() throws Exception {
        given()
                .when()
                .contentType(ContentType.JSON)
                .content("{\"value\":\"Hello, world!\"}")
                .post(server.url("/echo"))
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .content("value", is("Hello, world!"));
    }

    @Test
    public void shouldSupportReadSingleByte() throws Exception {
        given()
                .when()
                .contentType(ContentType.JSON)
                .content("{\"value\":\"Hello, world!\"}")
                .post(server.url("/echo?mode=byte"))
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .content("value", is("Hello, world!"));
    }

    @Test
    public void shouldSupportReadByte() throws Exception {
        given()
                .when()
                .contentType(ContentType.JSON)
                .content("{\"value\":\"Hello, world!\"}")
                .post(server.url("/echo"))
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .content("value", is("Hello, world!"));
    }

    @Test
    public void shouldSupportReadBytes() throws Exception {
        given()
                .when()
                .contentType(ContentType.JSON)
                .content("{\"value\":\"Hello, world!\"}")
                .post(server.url("/echo?mode=bytes"))
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .content("value", is("Hello, world!"));
    }

    @Test
    public void shouldSupportStream() throws Exception {
        given()
                .when()
                .contentType(ContentType.JSON)
                .content("{\"value\":\"Hello, world!\"}")
                .post(server.url("/echo?mode=stream"))
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .content("value", is("Hello, world!"));
    }

    @Test
    public void shouldSupportReader() throws Exception {
        given()
                .when()
                .contentType(ContentType.JSON)
                .content("{\"value\":\"Hello, world!\"}")
                .post(server.url("/echo?mode=writer"))
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .content("value", is("Hello, world!"));
    }

}
