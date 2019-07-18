package org.zalando.logbook.json;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.MockHeaders;
import org.zalando.logbook.MockHttpRequest;
import org.zalando.logbook.MockHttpResponse;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.time.Clock.systemUTC;
import static java.time.Duration.ZERO;
import static java.time.Duration.ofMillis;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.not;
import static org.zalando.logbook.Origin.LOCAL;
import static org.zalando.logbook.Origin.REMOTE;

final class JsonHttpLogFormatterTest {

    private final HttpLogFormatter unit = new JsonHttpLogFormatter();

    @Test
    void shouldLogRequest() throws IOException {
        final String correlationId = "3ce91230-677b-11e5-87b7-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(REMOTE)
                .withPath("/test")
                .withQuery("limit=1")
                .withHeaders(MockHeaders.of(
                        "Accept", "application/json",
                        "Date", "Tue, 15 Nov 1994 08:12:31 GMT"))
                .withContentType("application/xml")
                .withBodyAsString("<action>test</action>");

        final String json = unit.format(new SimplePrecorrelation(correlationId, systemUTC()), request);

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
    void shouldLogRequestWithoutHeaders() throws IOException {
        final String correlationId = "b7e7a488-682a-11e5-b527-10ddb1ee7671\n";
        final HttpRequest request = MockHttpRequest.create()
                .withPath("/test")
                .withBodyAsString("Hello, world!");

        final String json = unit.format(new SimplePrecorrelation(correlationId, systemUTC()), request);

        with(json)
                .assertThat("$", not(hasKey("headers")));
    }

    @Test
    void shouldLogRequestWithoutContentType() throws IOException {
        final String correlationId = "3ce91230-677b-11e5-87b7-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(REMOTE)
                .withPath("/test")
                .withBodyAsString("Hello");

        final String json = unit.format(new SimplePrecorrelation(correlationId, systemUTC()), request);

        with(json)
                .assertThat("$.origin", is("remote"))
                .assertThat("$.type", is("request"))
                .assertThat("$.correlation", is("3ce91230-677b-11e5-87b7-10ddb1ee7671"))
                .assertThat("$.protocol", is("HTTP/1.0"))
                .assertThat("$.remote", is("127.0.0.1"))
                .assertThat("$.method", is("GET"))
                .assertThat("$.uri", is("http://localhost/test"))
                .assertThat("$.body", is("Hello"));
    }

    @Test
    void shouldLogRequestWithoutBody() throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("")
                .withBodyAsString("");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertThat("$", not(hasKey("body")));
    }

    @Test
    void shouldEmbedJsonRequestBody() throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("application/json")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertThat("$.body.name", is("Bob"));
    }

    @Test
    void shouldEmbedInvalidJsonRequestBody() throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("application/json")
                .withBodyAsString("{\"name\":\"Bob\"};");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        assertThat(json, containsString("{\"name\":\"Bob\"};"));
    }

    @Test
    void shouldNotEmbedReplacedJsonRequestBody() throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("application/json")
                .withBodyAsString("<skipped>");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertThat("$.body", is("<skipped>"));
    }

    @Test
    void shouldEmbedCustomJsonRequestBody() throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("application/custom+json")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertThat("$.body.name", is("Bob"));
    }

    @Test
    void shouldEmbedCustomJsonWithParametersRequestBody() throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("application/custom+json; version=2")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertThat("$.body.name", is("Bob"));
    }

    @Test
    void shouldNotEmbedCustomTextXmlRequestBody() throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("text/xml")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertThat("$.body", is("{\"name\":\"Bob\"}"));
    }

    @Test
    void shouldNotEmbedInvalidContentTypeRequestBody() throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("x;y/z")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertThat("$.body", is("{\"name\":\"Bob\"}"));
    }

    @Test
    void shouldNotEmbedCustomTextJsonRequestBody() throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("text/custom+json")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertThat("$.body", is("{\"name\":\"Bob\"}"));
    }

    @Test
    void shouldNotEmbedNonJsonRequestBody() throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("application/not-json")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertThat("$.body", is("{\"name\":\"Bob\"}"));
    }

    @Test
    void shouldNotEmbedEmptyJsonRequestBody() throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("application/json");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        assertThat(json, not(containsString("\"body\"")));
    }

    @Test
    void shouldLogResponse() throws IOException {
        final String correlationId = "53de2640-677d-11e5-bc84-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(LOCAL)
                .withHeaders(MockHeaders.of("Date", "Tue, 15 Nov 1994 08:12:31 GMT"))
                .withContentType("application/xml")
                .withBodyAsString("<success>true<success>");

        final String json = unit.format(new SimpleCorrelation(correlationId, ofMillis(125)), response);

        with(json)
                .assertThat("$.origin", is("local"))
                .assertThat("$.type", is("response"))
                .assertThat("$.correlation", is("53de2640-677d-11e5-bc84-10ddb1ee7671"))
                .assertThat("$.protocol", is("HTTP/1.0"))
                .assertThat("$.status", is(200))
                .assertThat("$.headers.*", hasSize(1))
                .assertThat("$.headers['Date']", is(singletonList("Tue, 15 Nov 1994 08:12:31 GMT")))
                .assertThat("$.body", is("<success>true<success>"))
                .assertThat("$.duration", is(125));
    }

    @Test
    void shouldLogResponseWithoutHeaders() throws IOException {
        final String correlationId = "f53ceee2-682a-11e5-a63e-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create();

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        with(json)
                .assertThat("$", not(hasKey("headers")));

    }

    @Test
    void shouldLogResponseWithoutBody() throws IOException {
        final String correlationId = "f238536c-682a-11e5-9bdd-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withBodyAsString("");

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        with(json)
                .assertThat("$", not(hasKey("body")));
    }

    @Test
    void shouldEmbedJsonResponseBodyAsIs() throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withContentType("application/json")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        with(json)
                .assertThat("$.body.name", is("Bob"));
    }

    @Test
    void shouldEmbedCustomJsonResponseBodyAsIs() throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withContentType("application/custom+json")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        with(json)
                .assertThat("$.body.name", is("Bob"));
    }

    @Test
    void shouldNotEmbedCustomTextJsonResponseBodyAsIs() throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withContentType("text/custom+json")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        with(json)
                .assertThat("$.body", is("{\"name\":\"Bob\"}"));
    }

    @Test
    void shouldNotEmbedJsonResponseBodyIfEmpty() throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withContentType("application/json");

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        assertThat(json, not(containsString("\"body\"")));
    }

    @Test
    void shouldEmbedInvalidJsonResponseBody() throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withContentType("application/json")
                .withBodyAsString("{\"name\":\"Bob\"};");

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        assertThat(json, containsString("{\"name\":\"Bob\"};"));
    }

    @Test
    void shouldNotEmbedInvalidButProbableJsonResponseBody() throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withContentType("application/json")
                .withBodyAsString("{\"name\":\"Bob\"\n;};");

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        assertThat(json, containsString("{\"name\":\"Bob\"\n;};"));
    }
    
    @Test
    void shouldLogCorrectRequestOrigin() throws IOException {
        final String correlationId = "53de2640-677d-11e5-bc84-10ddb1ee7671";
        final HttpRequest local = MockHttpRequest.create().withOrigin(LOCAL);
        final String localJson = unit.format(new SimplePrecorrelation(correlationId, systemUTC()), local);

        with(localJson)
                .assertThat("$.origin", is("local"));
        
        final HttpRequest remote = MockHttpRequest.create().withOrigin(REMOTE);
        final String remoteJson = unit.format(new SimplePrecorrelation(correlationId, systemUTC()), remote);

        with(remoteJson)
                .assertThat("$.origin", is("remote"));
        
    }       
    
    @Test
    void shouldLogCorrectResponseOrigin() throws IOException {
        final String correlationId = "53de2640-677d-11e5-bc84-10ddb1ee7671";
        final HttpResponse local = MockHttpResponse.create().withOrigin(LOCAL);
        final String localJson = unit.format(new SimpleCorrelation(correlationId, ofMillis(125)), local);

        with(localJson)
                .assertThat("$.origin", is("local"));
        
        final HttpResponse remote = MockHttpResponse.create().withOrigin(REMOTE);
        final String remoteJson = unit.format(new SimpleCorrelation(correlationId, ofMillis(125)), remote);

        with(remoteJson)
                .assertThat("$.origin", is("remote"));
        
    }
    
    @Test
    void shouldWorkWithSpecialCharacters() throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withContentType("application/json")
                .withHeaders(MockHeaders.of("X-Nordic-Text", "ØÆÅabc\\\""))
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        with(json)
            .assertThat("$.headers['X-Nordic-Text']", is(singletonList("ØÆÅabc\\\"")));
    }
    
    @Test
    void shouldWorkLogEmptyHeaders() throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        
        Map<String, List<String>> headers = new TreeMap<>();
        headers.put("Content-Type", Arrays.asList("application/json"));
        headers.put("X-Empty-Header", Collections.emptyList());
        
        final HttpResponse response = MockHttpResponse.create()
                .withContentType("application/json")
                .withHeaders(headers)
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        with(json)
            .assertThat("$.headers['X-Empty-Header']", is(emptyCollectionOf(List.class)));
    }

    @Test
    void shouldLogNonstandardHttpPort() throws IOException {
        final String correlationId = "3ce91230-677b-11e5-87b7-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(REMOTE)
                .withPort(Optional.of(123))
                .withPath("/test");

        final String json = unit.format(new SimplePrecorrelation(correlationId, systemUTC()), request);

        with(json)
                .assertThat("$.uri", is("http://localhost:123/test"));
    }
    
    @Test
    void shouldLogNonstandardHttpsPort() throws IOException {
        final String correlationId = "3ce91230-677b-11e5-87b7-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withProtocolVersion("HTTPS/1.0")
                .withScheme("https")
                .withOrigin(REMOTE)
                .withPort(Optional.of(123))
                .withPath("/test");

        final String json = unit.format(new SimplePrecorrelation(correlationId, systemUTC()), request);

        with(json)
                .assertThat("$.uri", is("https://localhost:123/test"));
    }    

    @Test
    void shouldNotLogStandardHttpsPort() throws IOException {
        final String correlationId = "3ce91230-677b-11e5-87b7-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withProtocolVersion("HTTPS/1.0")
                .withScheme("https")
                .withOrigin(REMOTE)
                .withPort(Optional.of(443))
                .withPath("/test");

        final String json = unit.format(new SimplePrecorrelation(correlationId, systemUTC()), request);

        with(json)
                .assertThat("$.uri", is("https://localhost/test"));
    }    

    @Test
    void shouldNotLogStandardHttpPort() throws IOException {
        final String correlationId = "3ce91230-677b-11e5-87b7-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withProtocolVersion("HTTP/1.0")
                .withScheme("http")
                .withOrigin(REMOTE)
                .withPort(Optional.of(80))
                .withPath("/test");

        final String json = unit.format(new SimplePrecorrelation(correlationId, systemUTC()), request);

        with(json)
                .assertThat("$.uri", is("http://localhost/test"));
    }

    @Test
    void shouldNotLogMissingPort() throws IOException {
        final String correlationId = "3ce91230-677b-11e5-87b7-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withProtocolVersion("HTTP/1.0")
                .withScheme("http")
                .withOrigin(REMOTE)
                .withPort(Optional.empty())
                .withPath("/test");

        final String json = unit.format(new SimplePrecorrelation(correlationId, systemUTC()), request);

        with(json)
                .assertThat("$.uri", is("http://localhost/test"));
    }
    
    
    static class SimplePrecorrelation implements Precorrelation {

        private final String id;
        private final Clock clock;
        private final Instant start;

        SimplePrecorrelation(final String id, final Clock clock) {
            this.id = id;
            this.clock = clock;
            this.start = Instant.now(clock);
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public Correlation correlate() {
            final Instant end = Instant.now(clock);
            final Duration duration = Duration.between(start, end);
            return new SimpleCorrelation(id, duration);
        }

    }
    
    @AllArgsConstructor
    static class SimpleCorrelation implements Correlation {

        private final String id;
        private final Duration duration;

        @Override
        public String getId() {
            return id;
        }

        @Override
        public Duration getDuration() {
            return duration;
        }

    }

}
