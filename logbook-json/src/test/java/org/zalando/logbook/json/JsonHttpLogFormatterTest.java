package org.zalando.logbook.json;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Configuration.Defaults;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.attributes.HttpAttributes;
import org.zalando.logbook.test.MockHttpRequest;
import org.zalando.logbook.test.MockHttpResponse;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.time.Clock.systemUTC;
import static java.time.Duration.ZERO;
import static java.time.Duration.ofMillis;
import static java.time.Instant.MIN;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.zalando.logbook.Origin.LOCAL;
import static org.zalando.logbook.Origin.REMOTE;

final class JsonHttpLogFormatterTest {

    @BeforeAll
    static void beforeAll() {
        Configuration.setDefaults(new Defaults() {

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }

            @Override
            public MappingProvider mappingProvider() {
                return new JacksonMappingProvider();
            }

            @Override
            public JsonProvider jsonProvider() {
                return new JacksonJsonProvider();
            }
        });
    }

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
                .assertEquals("$.origin", "remote")
                .assertEquals("$.type", "request")
                .assertEquals("$.correlation", "3ce91230-677b-11e5-87b7-10ddb1ee7671")
                .assertEquals("$.protocol", "HTTP/1.0")
                .assertEquals("$.remote", "127.0.0.1")
                .assertEquals("$.method", "GET")
                .assertEquals("$.uri", "http://localhost/test?limit=1")
                // TODO .assertThat("$.headers.*", hasSize(2))
                .assertEquals("$.headers['Accept']", singletonList("application/json"))
                .assertEquals("$.headers['Date']", singletonList("Tue, 15 Nov 1994 08:12:31 GMT"))
                .assertEquals("$.body", "<action>test</action>");
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
                .assertNotDefined("$.headers");
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
                .assertEquals("$.origin", "remote")
                .assertEquals("$.type", "request")
                .assertEquals("$.correlation", "3ce91230-677b-11e5-87b7-10ddb1ee7671")
                .assertEquals("$.protocol", "HTTP/1.0")
                .assertEquals("$.remote", "127.0.0.1")
                .assertEquals("$.method", "GET")
                .assertEquals("$.uri", "http://localhost/test")
                .assertEquals("$.body", "Hello");
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldLogRequestWithoutBody(final HttpLogFormatter unit) throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("")
                .withBodyAsString("");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertNotDefined("$.body");
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldEmbedJsonRequestBody(final HttpLogFormatter unit) throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("application/json")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertEquals("$.body.name", "Bob");
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldEmbedInvalidJsonRequestBody(final HttpLogFormatter unit) throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("application/json")
                .withBodyAsString("{\"name\":\"Bob\"};");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        assertThat(json)
                .contains("{\"name\":\"Bob\"};");
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldNotEmbedReplacedJsonRequestBody(final HttpLogFormatter unit) throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("application/json")
                .withBodyAsString("\"<skipped>\"");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertEquals("$.body", "<skipped>");
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldEmbedCustomJsonRequestBody(final HttpLogFormatter unit) throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("application/custom+json")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertEquals("$.body.name", "Bob");
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldEmbedCustomJsonWithParametersRequestBody(final HttpLogFormatter unit) throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("application/custom+json; version=2")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertEquals("$.body.name", "Bob");
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldNotEmbedCustomTextXmlRequestBody(final HttpLogFormatter unit) throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("text/xml")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertEquals("$.body", "{\"name\":\"Bob\"}");
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldNotEmbedInvalidContentTypeRequestBody(final HttpLogFormatter unit) throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("x;y/z")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertEquals("$.body", "{\"name\":\"Bob\"}");
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldNotEmbedCustomTextJsonRequestBody(final HttpLogFormatter unit) throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("text/custom+json")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertEquals("$.body", "{\"name\":\"Bob\"}");
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldNotEmbedNonJsonRequestBody(final HttpLogFormatter unit) throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("application/not-json")
                .withBodyAsString("{\"name\":\"Bob\"}");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        with(json)
                .assertEquals("$.body", "{\"name\":\"Bob\"}");
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldNotEmbedEmptyJsonRequestBody(final HttpLogFormatter unit) throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withContentType("application/json");

        final String json = unit.format(new SimplePrecorrelation("", systemUTC()), request);

        assertThat(json).doesNotContain("\"body\"");
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
                .assertEquals("$.origin", "local")
                .assertEquals("$.type", "response")
                .assertEquals("$.correlation", "53de2640-677d-11e5-bc84-10ddb1ee7671")
                .assertEquals("$.protocol", "HTTP/1.0")
                .assertEquals("$.status", 200)
                .assertEquals("$.headers", singletonMap("Date", singletonList("Tue, 15 Nov 1994 08:12:31 GMT")))
                .assertEquals("$.body", "<success>true<success>")
                .assertEquals("$.duration", 125);
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldLogResponseWithoutHeaders(final HttpLogFormatter unit) throws IOException {
        final String correlationId = "f53ceee2-682a-11e5-a63e-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create();

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        with(json)
                .assertNotDefined("$.headers");
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldLogResponseWithoutBody(final HttpLogFormatter unit) throws IOException {
        final String correlationId = "f238536c-682a-11e5-9bdd-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withBodyAsString("");

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        with(json)
                .assertNotDefined("$.body");
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
                .assertEquals("$.body.name", "Bob");
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
                .assertEquals("$.body.name", "Bob");
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
                .assertEquals("$.body", "{\"name\":\"Bob\"}");
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldNotEmbedJsonResponseBodyIfEmpty(final HttpLogFormatter unit) throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withContentType("application/json");

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        assertThat(json).doesNotContain("\"body\"");
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldEmbedInvalidJsonResponseBody(final HttpLogFormatter unit) throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withContentType("application/json")
                .withBodyAsString("{\"name\":\"Bob\"};");

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        assertThat(json).contains("{\"name\":\"Bob\"};");
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldNotEmbedInvalidButProbableJsonResponseBody(final HttpLogFormatter unit) throws IOException {
        final String correlationId = "5478b8da-6d87-11e5-a80f-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withContentType("application/json")
                .withBodyAsString("{\"name\":\"Bob\"\n;};");

        final String json = unit.format(new SimpleCorrelation(correlationId, ZERO), response);

        assertThat(json).contains("{\"name\":\"Bob\"\n;};");
    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldLogCorrectRequestOrigin(final HttpLogFormatter unit) throws IOException {
        final String correlationId = "53de2640-677d-11e5-bc84-10ddb1ee7671";
        final HttpRequest local = MockHttpRequest.create().withOrigin(LOCAL);
        final String localJson = unit.format(new SimplePrecorrelation(correlationId, systemUTC()), local);

        with(localJson)
                .assertEquals("$.origin", "local");

        final HttpRequest remote = MockHttpRequest.create().withOrigin(REMOTE);
        final String remoteJson = unit.format(new SimplePrecorrelation(correlationId, systemUTC()), remote);

        with(remoteJson)
                .assertEquals("$.origin", "remote");

    }

    @ParameterizedTest
    @MethodSource("units")
    void shouldLogCorrectResponseOrigin(final HttpLogFormatter unit) throws IOException {
        final String correlationId = "53de2640-677d-11e5-bc84-10ddb1ee7671";
        final HttpResponse local = MockHttpResponse.create().withOrigin(LOCAL);
        final String localJson = unit.format(new SimpleCorrelation(correlationId, ofMillis(125)), local);

        with(localJson)
                .assertEquals("$.origin", "local");

        final HttpResponse remote = MockHttpResponse.create().withOrigin(REMOTE);
        final String remoteJson = unit.format(new SimpleCorrelation(correlationId, ofMillis(125)), remote);

        with(remoteJson)
                .assertEquals("$.origin", "remote");

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
                .assertEquals("$.headers['X-Nordic-Text']", singletonList("ØÆÅabc\\\""));
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
                .assertEquals("$.headers['X-Empty-Header']", emptyList());
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
                .assertEquals("$.uri", "http://localhost:123/test");
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
                .assertEquals("$.uri", "https://localhost:123/test");
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
                .assertEquals("$.uri", "https://localhost/test");
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
                .assertEquals("$.uri", "http://localhost/test");
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
                .assertEquals("$.uri", "http://localhost/test");
    }

    @Test
    void shouldLogRequestAttributes() throws IOException {
        final HttpLogFormatter unit = new JsonHttpLogFormatter();
        final PersonAttributeDto person = new PersonAttributeDto("Bob", 42);
        final Map<String, Object> personMap = new HashMap<>();
        personMap.put("name", "Bob");
        personMap.put("age", 42);

        final HttpRequest request = MockHttpRequest.create().withHttpAttributes(HttpAttributes.of("person1", person));
        final HttpResponse response = MockHttpResponse.create().withHttpAttributes(HttpAttributes.of("person2", person));

        final String requestJson = unit.format(new SimplePrecorrelation("", systemUTC()), request);
        final String responseJson = unit.format(new SimpleCorrelation("", ofMillis(125)), response);

        Map<String, Object> requestAttributes = JsonPath.read(requestJson, "$.attributes");
        assertThat(requestAttributes).hasSize(1);
        assertThat(requestAttributes).containsEntry("person1", personMap);

        Map<String, Object> responseAttributes = JsonPath.read(responseJson, "$.attributes");
        assertThat(responseAttributes).hasSize(1);
        assertThat(responseAttributes).containsEntry("person2", personMap);
    }

    // A simple DTO to show that the JSON formatter can handle arbitrary objects for attribute values
    @Data
    @AllArgsConstructor
    private static class PersonAttributeDto {
        private String name;
        private int age;
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
