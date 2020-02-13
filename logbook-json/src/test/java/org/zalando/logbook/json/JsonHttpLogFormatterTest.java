package org.zalando.logbook.json;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.MockHttpRequest;
import org.zalando.logbook.MockHttpResponse;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.time.Clock.systemUTC;
import static java.time.Duration.ZERO;
import static java.time.Duration.ofMillis;
import static java.time.Instant.MIN;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.zalando.logbook.Origin.LOCAL;
import static org.zalando.logbook.Origin.REMOTE;

final class JsonHttpLogFormatterTest {

    @MethodSource
    static Iterable<HttpLogFormatter> units() {
        return Arrays.asList(
                new JsonHttpLogFormatter(),
                new FastJsonHttpLogFormatter()
        );
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldLogRequest(final HttpLogFormatter unit) throws IOException {
        final String correlationId = "3ce91230-677b-11e5-87b7-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(REMOTE)
                .withPath("/test")
                .withQuery("limit=1")
                .withHeaders(HttpHeaders.empty()
                        .update("Accept", "application/json")
                        .update("Date", "Tue, 15 Nov 1994 08:12:31 GMT"))
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

    @ParameterizedTest
    @MethodSource("units")
    void shouldLogRequestWithoutHeaders(final HttpLogFormatter unit) throws IOException {
        final String correlationId = "b7e7a488-682a-11e5-b527-10ddb1ee7671\n";
        final HttpRequest request = MockHttpRequest.create()
                .withPath("/test")
                .withBodyAsString("Hello, world!");

        final String json = unit.format(new SimplePrecorrelation(correlationId, systemUTC()), request);

        with(json)
                .assertThat("$", not(hasKey("headers")));
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldLogRequestWithoutContentType(final HttpLogFormatter unit) throws IOException {
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

    @ParameterizedTest
    @MethodSource("units")
    void shouldLogRequestWithoutBody(final HttpLogFormatter unit) throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("")
                .withBodyAsString("");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertThat("$", not(hasKey("body")));
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldEmbedJsonRequestBody(final HttpLogFormatter unit) throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("application/json")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertThat("$.body.name", is("Bob"));
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldEmbedInvalidJsonRequestBody(final HttpLogFormatter unit) throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("application/json")
                .withBodyAsString("{\"name\":\"Bob\"};");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        assertThat(json, containsString("{\"name\":\"Bob\"};"));
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldNotEmbedReplacedJsonRequestBody(final HttpLogFormatter unit) throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("application/json")
                .withBodyAsString("<skipped>");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertThat("$.body", is("<skipped>"));
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldEmbedCustomJsonRequestBody(final HttpLogFormatter unit) throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("application/custom+json")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertThat("$.body.name", is("Bob"));
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldEmbedCustomJsonWithParametersRequestBody(final HttpLogFormatter unit) throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("application/custom+json; version=2")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertThat("$.body.name", is("Bob"));
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldNotEmbedCustomTextXmlRequestBody(final HttpLogFormatter unit) throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("text/xml")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertThat("$.body", is("{\"name\":\"Bob\"}"));
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldNotEmbedInvalidContentTypeRequestBody(final HttpLogFormatter unit) throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("x;y/z")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertThat("$.body", is("{\"name\":\"Bob\"}"));
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldNotEmbedCustomTextJsonRequestBody(final HttpLogFormatter unit) throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("text/custom+json")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertThat("$.body", is("{\"name\":\"Bob\"}"));
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldNotEmbedNonJsonRequestBody(final HttpLogFormatter unit) throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("application/not-json")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertThat("$.body", is("{\"name\":\"Bob\"}"));
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldNotEmbedEmptyJsonRequestBody(final HttpLogFormatter unit) throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("application/json");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        assertThat(json, not(containsString("\"body\"")));
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldLogResponse(final HttpLogFormatter unit) throws IOException {
        final String correlationId = "53de2640-677d-11e5-bc84-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(LOCAL)
                .withHeaders(HttpHeaders.of(
                        "Date", "Tue, 15 Nov 1994 08:12:31 GMT"))
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

    @ParameterizedTest
    @MethodSource("units")
    void shouldLogResponseWithoutHeaders(final HttpLogFormatter unit) throws IOException {
        final String correlationId = "f53ceee2-682a-11e5-a63e-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create();

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        with(json)
                .assertThat("$", not(hasKey("headers")));

    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldLogResponseWithoutBody(final HttpLogFormatter unit) throws IOException {
        final String correlationId = "f238536c-682a-11e5-9bdd-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withBodyAsString("");

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        with(json)
                .assertThat("$", not(hasKey("body")));
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldEmbedJsonResponseBodyAsIs(final HttpLogFormatter unit) throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withContentType("application/json")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        with(json)
                .assertThat("$.body.name", is("Bob"));
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldEmbedCustomJsonResponseBodyAsIs(final HttpLogFormatter unit) throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withContentType("application/custom+json")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        with(json)
                .assertThat("$.body.name", is("Bob"));
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldNotEmbedCustomTextJsonResponseBodyAsIs(final HttpLogFormatter unit) throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withContentType("text/custom+json")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        with(json)
                .assertThat("$.body", is("{\"name\":\"Bob\"}"));
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldNotEmbedJsonResponseBodyIfEmpty(final HttpLogFormatter unit) throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withContentType("application/json");

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        assertThat(json, not(containsString("\"body\"")));
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldEmbedInvalidJsonResponseBody(final HttpLogFormatter unit) throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withContentType("application/json")
                .withBodyAsString("{\"name\":\"Bob\"};");

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        assertThat(json, containsString("{\"name\":\"Bob\"};"));
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldNotEmbedInvalidButProbableJsonResponseBody(final HttpLogFormatter unit) throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withContentType("application/json")
                .withBodyAsString("{\"name\":\"Bob\"\n;};");

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        assertThat(json, containsString("{\"name\":\"Bob\"\n;};"));
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldLogCorrectRequestOrigin(final HttpLogFormatter unit) throws IOException {
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

    @ParameterizedTest
    @MethodSource("units")
    void shouldLogCorrectResponseOrigin(final HttpLogFormatter unit) throws IOException {
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

    @ParameterizedTest
    @MethodSource("units")
    void shouldWorkWithSpecialCharacters(final HttpLogFormatter unit) throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withContentType("application/json")
                .withHeaders(HttpHeaders.of("X-Nordic-Text", "ØÆÅabc\\\""))
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        with(json)
                .assertThat("$.headers['X-Nordic-Text']", is(singletonList("ØÆÅabc\\\"")));
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldWorkLogEmptyHeaders(final HttpLogFormatter unit) throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";

        final HttpResponse response = MockHttpResponse.create()
                .withContentType("application/json")
                .withHeaders(HttpHeaders.empty()
                        .update("Content-Type", "application/json")
                        .update("X-Empty-Header"))
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        with(json)
                .assertThat("$.headers['X-Empty-Header']", is(emptyCollectionOf(List.class)));
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldLogNonstandardHttpPort(final HttpLogFormatter unit) throws IOException {
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

    @ParameterizedTest
    @MethodSource("units")
    void shouldLogNonstandardHttpsPort(final HttpLogFormatter unit) throws IOException {
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

    @ParameterizedTest
    @MethodSource("units")
    void shouldNotLogStandardHttpsPort(final HttpLogFormatter unit) throws IOException {
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

    @ParameterizedTest
    @MethodSource("units")
    void shouldNotLogStandardHttpPort(final HttpLogFormatter unit) throws IOException {
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

    @ParameterizedTest
    @MethodSource("units")
    void shouldNotLogMissingPort(final HttpLogFormatter unit) throws IOException {
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


    @Getter
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
        public Correlation correlate() {
            final Instant end = Instant.now(clock);
            final Duration duration = Duration.between(start, end);
            return new SimpleCorrelation(id, start, end, duration);
        }

    }

    @Getter
    @AllArgsConstructor
    private static class SimpleCorrelation implements Correlation {

        private final String id;
        private final Instant start;
        private final Instant end;
        private final Duration duration;

        SimpleCorrelation(final String id, final Duration duration) {
            this(id, MIN, MIN.plus(duration), duration);
        }

    }

}
