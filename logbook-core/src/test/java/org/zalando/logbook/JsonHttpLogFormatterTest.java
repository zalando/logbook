package org.zalando.logbook;

/*
 * #%L
 * Logbook: Core
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

import com.google.common.collect.ImmutableListMultimap;
import org.junit.Test;
import org.zalando.logbook.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.zalando.logbook.MockHttpRequest.request;
import static org.zalando.logbook.MockHttpResponse.response;
import static org.zalando.logbook.Origin.LOCAL;
import static org.zalando.logbook.Origin.REMOTE;

public final class JsonHttpLogFormatterTest {

    private final HttpLogFormatter unit = new JsonHttpLogFormatter();

    @Test
    public void shouldLogRequest() throws IOException {
        final String correlationId = "3ce91230-677b-11e5-87b7-10ddb1ee7671";
        final HttpRequest request = request()
                .protocolVersion("HTTP/1.0")
                .origin(REMOTE)
                .path("/test")
                .query("limit=1")
                .headers(ImmutableListMultimap.of(
                        "Accept", "application/json",
                        "Date", "Tue, 15 Nov 1994 08:12:31 GMT"))
                .contentType("application/xml")
                .body("<action>test</action>")
                .build();

        final String json = unit.format(new SimplePrecorrelation<>(correlationId, request));

        with(json)
                .assertThat("$.origin", is("remote"))
                .assertThat("$.type", is("request"))
                .assertThat("$.correlation", is("3ce91230-677b-11e5-87b7-10ddb1ee7671"))
                .assertThat("$.protocol", is("HTTP/1.0"))
                .assertThat("$.remote", is("127.0.0.1"))
                .assertThat("$.method", is("GET"))
                .assertThat("$.uri", is("http://localhost/test?limit=1"))
                .assertThat("$.headers.*", hasSize(2))
                .assertThat("$.headers['Accept']", is(singletonList("application/json")))
                .assertThat("$.headers['Date']", is(singletonList("Tue, 15 Nov 1994 08:12:31 GMT")))
                .assertThat("$.body", is("<action>test</action>"));
    }

    @Test
    public void shouldLogRequestWithoutHeaders() throws IOException {
        final String correlationId = "b7e7a488-682a-11e5-b527-10ddb1ee7671\n";
        final HttpRequest request = request()
                .path("/test")
                .body("Hello, world!")
                .build();

        final String json = unit.format(new SimplePrecorrelation<>(correlationId, request));

        with(json)
                .assertThat("$", not(hasKey("headers")));
    }

    @Test
    public void shouldLogRequestWithoutBody() throws IOException {
        final String correlationId = "ac5c3dc2-682a-11e5-83cd-10ddb1ee7671";
        final HttpRequest request = request()
                .body("")
                .build();

        final String json = unit.format(new SimplePrecorrelation<>(correlationId, request));

        with(json)
                .assertThat("$", not(hasKey("body")));
    }

    @Test
    public void shouldEmbedJsonRequestBodyAsIs() throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpRequest request = request()
                .contentType("application/json")
                .body("{\"name\":\"Bob\"}")
                .build();

        final String json = unit.format(new SimplePrecorrelation<>(correlationId, request));

        with(json)
                .assertThat("$.body.name", is("Bob"));
    }

    @Test
    public void shouldLogInvalidJsonRequestBodyAsIs() throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpRequest request = request()
                .contentType("application/json")
                .body("{\n \"name\":\"Bob\";;;\n;}")
                .build();

        final String json = unit.format(new SimplePrecorrelation<>(correlationId, request));

        with(json)
                .assertThat("$.body", is("{\n \"name\":\"Bob\";;;\n;}"));
    }

    @Test
    public void shouldCompactEmbeddedJsonRequestBody() throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpRequest request = request()
                .contentType("application/json")
                .body("{\n  \"name\": \"Bob\"\n}")
                .build();

        final String json = unit.format(new SimplePrecorrelation<>(correlationId, request));

        assertThat(json, containsString("{\"name\":\"Bob\"}"));
    }

    @Test
    public void shouldEmbedCustomJsonRequestBodyAsIs() throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpRequest request = request()
                .contentType("application/custom+json")
                .body("{\"name\":\"Bob\"}")
                .build();

        final String json = unit.format(new SimplePrecorrelation<>(correlationId, request));

        with(json)
                .assertThat("$.body.name", is("Bob"));
    }

    @Test
    public void shouldNotEmbedCustomTextJsonRequestBodyAsIs() throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpRequest request = request()
                .contentType("text/custom+json")
                .body("{\"name\":\"Bob\"}")
                .build();

        final String json = unit.format(new SimplePrecorrelation<>(correlationId, request));

        with(json)
                .assertThat("$.body", is("{\"name\":\"Bob\"}"));
    }

    @Test
    public void shouldEmbedJsonRequestBodyAsNullIfEmpty() throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpRequest request = request()
                .contentType("application/json")
                .build();

        final String json = unit.format(new SimplePrecorrelation<>(correlationId, request));

        with(json)
                .assertThat("$.body", is(emptyString()));
    }

    @Test
    public void shouldLogResponse() throws IOException {
        final String correlationId = "53de2640-677d-11e5-bc84-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create();
        final HttpResponse response = response()
                .protocolVersion("HTTP/1.0")
                .origin(LOCAL)
                .headers(ImmutableListMultimap.of("Date", "Tue, 15 Nov 1994 08:12:31 GMT"))
                .contentType("application/xml")
                .body("<success>true<success>")
                .build();

        final String json = unit.format(new SimpleCorrelation<>(correlationId, request, response));

        with(json)
                .assertThat("$.origin", is("local"))
                .assertThat("$.type", is("response"))
                .assertThat("$.correlation", is("53de2640-677d-11e5-bc84-10ddb1ee7671"))
                .assertThat("$.protocol", is("HTTP/1.0"))
                .assertThat("$.status", is(200))
                .assertThat("$.headers.*", hasSize(1))
                .assertThat("$.headers['Date']", is(singletonList("Tue, 15 Nov 1994 08:12:31 GMT")))
                .assertThat("$.body", is("<success>true<success>"));
    }

    @Test
    public void shouldLogResponseWithoutHeaders() throws IOException {
        final String correlationId = "f53ceee2-682a-11e5-a63e-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create();
        final HttpResponse response = MockHttpResponse.create();

        final String json = unit.format(new SimpleCorrelation<>(correlationId, request, response));

        with(json)
                .assertThat("$", not(hasKey("headers")));

    }

    @Test
    public void shouldLogResponseWithoutBody() throws IOException {
        final String correlationId = "f238536c-682a-11e5-9bdd-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create();
        final HttpResponse response = response()
                .body("")
                .build();

        final String json = unit.format(new SimpleCorrelation<>(correlationId, request, response));

        with(json)
                .assertThat("$", not(hasKey("body")));
    }

    @Test
    public void shouldEmbedJsonResponseBodyAsIs() throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create();
        final HttpResponse response = response()
                .contentType("application/json")
                .body("{\"name\":\"Bob\"}")
                .build();

        final String json = unit.format(new SimpleCorrelation<>(correlationId, request, response));

        with(json)
                .assertThat("$.body.name", is("Bob"));
    }

    @Test
    public void shouldCompactEmbeddedJsonResponseBody() throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create();
        final HttpResponse response = response()
                .contentType("application/json")
                .body("{\n  \"name\": \"Bob\"\n}")
                .build();

        final String json = unit.format(new SimpleCorrelation<>(correlationId, request, response));

        assertThat(json, containsString("{\"name\":\"Bob\"}"));
    }

    @Test
    public void shouldEmbedCustomJsonResponseBodyAsIs() throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create();
        final HttpResponse response = response()
                .contentType("application/custom+json")
                .body("{\"name\":\"Bob\"}")
                .build();

        final String json = unit.format(new SimpleCorrelation<>(correlationId, request, response));

        with(json)
                .assertThat("$.body.name", is("Bob"));
    }

    @Test
    public void shouldNotEmbedCustomTextJsonResponseBodyAsIs() throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create();
        final HttpResponse response = response()
                .contentType("text/custom+json")
                .body("{\"name\":\"Bob\"}")
                .build();

        final String json = unit.format(new SimpleCorrelation<>(correlationId, request, response));

        with(json)
                .assertThat("$.body", is("{\"name\":\"Bob\"}"));
    }

    @Test
    public void shouldEmbedJsonResponseBodyAsNullIfEmpty() throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create();
        final HttpResponse response = response()
                .contentType("application/json")
                .build();

        final String json = unit.format(new SimpleCorrelation<>(correlationId, request, response));

        with(json)
                .assertThat("$.body", is(emptyString()));
    }

    @Test
    public void shouldLogInvalidJsonResponseBodyAsIs() throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create();
        final HttpResponse response = response()
                .contentType("text/custom+json")
                .body("{\n \"name\":\"Bob\";;;\n;}")
                .build();

        final String json = unit.format(new SimpleCorrelation<>(correlationId, request, response));

        with(json)
                .assertThat("$.body", is("{\n \"name\":\"Bob\";;;\n;}"));
    }


}