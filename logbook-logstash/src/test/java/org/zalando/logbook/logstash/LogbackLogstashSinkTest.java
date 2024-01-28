package org.zalando.logbook.logstash;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Configuration.Defaults;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.json.JsonHttpLogFormatter;
import org.zalando.logbook.test.MockHttpRequest;
import org.zalando.logbook.test.MockHttpResponse;

import java.io.IOException;
import java.time.Duration;
import java.util.EnumSet;
import java.util.Set;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.Origin.REMOTE;

/**
 * Test request and response logging with and without pretty-printing.
 * <br/><br/>
 * Note: Use {@linkplain ch.qos.logback.core.util.StatusPrinter} to detect any Logback errors,
 * like for example in <a href="https://jira.qos.ch/browse/LOGBACK-615">here</a>.
 */

class LogbackLogstashSinkTest {

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

    // To make tests pass on Windows, cleaning up after each test is required
    @AfterEach
    void cleanUp() {
        PrettyPrintingStaticAppender.reset();
    }

    @AfterAll
    static void tearDown() {
        StaticAppender.reset();
    }

    @Test
    void shouldLogRequestAndResponse() throws IOException {
        logsAs("http");
    }

    @Test
    void shouldLogCorrectJsonForEmptyBody() throws IOException {
        final String correlationId = "3ce91230-677b-11e5-87b7-10ddb1ee7671";
        final int duration = 125;

        final Precorrelation precorrelation = mock(Precorrelation.class);
        final Correlation correlation = mock(Correlation.class);

        when(precorrelation.getId()).thenReturn(correlationId);
        when(correlation.getId()).thenReturn(correlationId);
        when(correlation.getDuration()).thenReturn(Duration.ofMillis(duration));

        final HttpLogFormatter formatter = new JsonHttpLogFormatter();
        final LogstashLogbackSink sink = new LogstashLogbackSink(formatter, Level.TRACE);

        assertTrue(sink.isActive());

        final HttpRequest request = MockHttpRequest.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(REMOTE)
                .withPath("/test")
                .withQuery("limit=1")
                .withHeaders(HttpHeaders.empty()
                        .update("Accept", "application/json")
                        .update("Date", "Tue, 15 Nov 1994 08:12:31 GMT"))
                .withContentType("application/json");

        sink.write(precorrelation, request);

        // check that actually pretty-printed - 8x space as deepest level
        final String prettyPrintedRequestStatement = PrettyPrintingStaticAppender.getLastStatement();

        for (final String last : new String[]{StaticAppender.getLastStatement(), prettyPrintedRequestStatement}) {
            with(last)
                    .assertEquals("$.message", request.getMethod() + " " + request.getRequestUri())
                    .assertNotDefined("$.http.body");
        }

        // This line is required to make tests pass on Windows
        PrettyPrintingStaticAppender.reset();

        final HttpResponse response = MockHttpResponse.create()
                .withStatus(200)
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(REMOTE)
                .withHeaders(HttpHeaders.of(
                        "Date", "Tue, 15 Nov 1994 08:12:31 GMT"))
                .withContentType("application/json");

        sink.write(correlation, request, response);

        // check that actually pretty-printed - 8x space as deepest level
        final String prettyPrintedResponseStatement = PrettyPrintingStaticAppender.getLastStatement();

        for (final String last : new String[]{StaticAppender.getLastStatement(), prettyPrintedResponseStatement}) {
            final String message = response.getStatus() + " " +
                    response.getReasonPhrase() + " " +
                    request.getMethod() + " " +
                    request.getRequestUri();

            with(last)
                    .assertEquals("$.message", message)
                    .assertNotDefined("$.http.body");
        }

    }

    @Test
    void shouldUsePassedBaseField() throws IOException {
        logsAs("test");
    }

    private void logsAs(final String baseFieldName) throws IOException {
        final String correlationId = "3ce91230-677b-11e5-87b7-10ddb1ee7671";
        final int duration = 125;

        final Precorrelation precorrelation = mock(Precorrelation.class);
        final Correlation correlation = mock(Correlation.class);

        when(precorrelation.getId()).thenReturn(correlationId);
        when(correlation.getId()).thenReturn(correlationId);
        when(correlation.getDuration()).thenReturn(Duration.ofMillis(duration));

        final HttpLogFormatter formatter = new JsonHttpLogFormatter();
        final LogstashLogbackSink sink = new LogstashLogbackSink(formatter, baseFieldName, Level.TRACE);

        assertTrue(sink.isActive());

        final HttpRequest request = MockHttpRequest.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(REMOTE)
                .withPath("/test")
                .withQuery("limit=1")
                .withHeaders(HttpHeaders.empty()
                        .update("Accept", "application/json")
                        .update("Date", "Tue, 15 Nov 1994 08:12:31 GMT"))
                .withContentType("application/json")
                .withBodyAsString("{\"person\":{\"name\":\"Thomas\"}}");

        sink.write(precorrelation, request);

        // check that actually pretty-printed - 8x space as deepest level
        final String prettyPrintedRequestStatement = PrettyPrintingStaticAppender.getLastStatement();
        assertThat(prettyPrintedRequestStatement).contains("\n        ");

        for (final String last : new String[]{StaticAppender.getLastStatement(), prettyPrintedRequestStatement}) {
            with(last)
                    .assertEquals("$.message", request.getMethod() + " " + request.getRequestUri())
                    .assertEquals("$." + baseFieldName + ".origin", "remote")
                    .assertEquals("$." + baseFieldName + ".type", "request")
                    .assertEquals("$." + baseFieldName + ".correlation", correlationId)
                    .assertEquals("$." + baseFieldName + ".protocol", "HTTP/1.0")
                    .assertEquals("$." + baseFieldName + ".remote", "127.0.0.1")
                    .assertEquals("$." + baseFieldName + ".method", "GET")
                    .assertEquals("$." + baseFieldName + ".uri", "http://localhost/test?limit=1")
                    // TODO .assertThat("$." + baseFieldName + ".headers.*", hasSize(2))
                    .assertEquals("$." + baseFieldName + ".headers['Accept']", singletonList("application/json"))
                    .assertEquals("$." + baseFieldName + ".headers['Date']", singletonList("Tue, 15 Nov 1994 08:12:31 GMT"))
                    .assertEquals("$." + baseFieldName + ".body.person.name", "Thomas");
        }

        // This line is required to make tests pass on Windows
        PrettyPrintingStaticAppender.reset();

        final HttpResponse response = MockHttpResponse.create()
                .withStatus(200)
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(REMOTE)
                .withHeaders(HttpHeaders.of("Date", "Tue, 15 Nov 1994 08:12:31 GMT"))
                .withContentType("application/json")
                .withBodyAsString("{\"person\":{\"name\":\"Magnus\"}}");

        sink.write(correlation, request, response);

        // check that actually pretty-printed - 8x space as deepest level
        final String prettyPrintedResponseStatement = PrettyPrintingStaticAppender.getLastStatement();
        assertThat(prettyPrintedResponseStatement).contains("\n        ");

        for (final String last : new String[]{StaticAppender.getLastStatement(), prettyPrintedResponseStatement}) {
            final String message = response.getStatus() + " " + response.getReasonPhrase() + " " + request.getMethod() + " " + request.getRequestUri();

            with(last)
                    .assertEquals("$.message", message)
                    .assertEquals("$." + baseFieldName + ".origin", "remote")
                    .assertEquals("$." + baseFieldName + ".type", "response")
                    .assertEquals("$." + baseFieldName + ".correlation", correlationId)
                    .assertEquals("$." + baseFieldName + ".protocol", "HTTP/1.0")
                    .assertEquals("$." + baseFieldName + ".status", 200)
                    .assertEquals("$." + baseFieldName + ".headers", singletonMap("Date", singletonList("Tue, 15 Nov 1994 08:12:31 GMT")))
                    .assertEquals("$." + baseFieldName + ".body.person.name", "Magnus")
                    .assertEquals("$." + baseFieldName + ".duration", duration);
        }

        // also verify for unknown http code
        final HttpResponse unknownStatusCodeResponse = MockHttpResponse.create()
                .withStatus(1000)
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(REMOTE)
                .withHeaders(HttpHeaders.of(
                        "Date", "Tue, 15 Nov 1994 08:12:31 GMT"))
                .withContentType("application/json")
                .withBodyAsString("{\"person\":{\"name\":\"Therese\"}}");

        sink.write(correlation, request, unknownStatusCodeResponse);

        final String message = unknownStatusCodeResponse.getStatus() + " " + request.getMethod() + " " + request.getRequestUri();

        with(StaticAppender.getLastStatement())
                .assertEquals("$.message", message)
                .assertEquals("$." + baseFieldName + ".status", 1000);
    }
}
