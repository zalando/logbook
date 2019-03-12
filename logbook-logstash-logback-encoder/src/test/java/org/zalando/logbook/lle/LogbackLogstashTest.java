package org.zalando.logbook.lle;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.Origin.REMOTE;

import java.io.IOException;
import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.MockHeaders;
import org.zalando.logbook.MockHttpRequest;
import org.zalando.logbook.MockHttpResponse;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.json.JsonHttpLogFormatter;

/**
 * Test request and response logging with and without pretty-printing.
 * <br/><br/>
 * Note: Use {@linkplain ch.qos.logback.core.util.StatusPrinter} to detect any Logback errors, 
 * like for example in https://jira.qos.ch/browse/LOGBACK-615
 *
 */

class LogbackLogstashTest {

    @AfterAll
    public static void tearDown() {
    	// clean up
        StaticAppender.reset();
        PrettyPrintingStaticAppender.reset();
    }

    @Test
    void shouldLogRequestAndResponse() throws IOException {
        String correlationId = "3ce91230-677b-11e5-87b7-10ddb1ee7671";
        int duration = 125;
        
    	Precorrelation precorrelation = mock(Precorrelation.class);
    	Correlation correlation = mock(Correlation.class);

    	when(precorrelation.getId()).thenReturn(correlationId);
    	when(correlation.getId()).thenReturn(correlationId);
    	when(correlation.getDuration()).thenReturn(Duration.ofMillis(duration));
    	
    	LogstashLogbackHttpLogWriter logWriter = new LogstashLogbackHttpLogWriter();
        HttpLogFormatter formatter = new JsonHttpLogFormatter();
        LogstashLogbackSink sink = new LogstashLogbackSink(formatter, logWriter);

        assertTrue(logWriter.isActive());
        
        final HttpRequest request = MockHttpRequest.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(REMOTE)
                .withPath("/test")
                .withQuery("limit=1")
                .withHeaders(MockHeaders.of(
                        "Accept", "application/json",
                        "Date", "Tue, 15 Nov 1994 08:12:31 GMT"))
                .withContentType("application/json")
                .withBodyAsString("{\"person\":{\"name\":\"Thomas\"}}");

        sink.write(precorrelation, request);

        for(String last : new String[] {StaticAppender.getLastStatement(), PrettyPrintingStaticAppender.getLastStatement()} ) {
            with(last)
                .assertThat("$.http.origin", is("remote"))
                .assertThat("$.http.type", is("request"))
                .assertThat("$.http.correlation", is(correlationId))
                .assertThat("$.http.protocol", is("HTTP/1.0"))
                .assertThat("$.http.remote", is("127.0.0.1"))
                .assertThat("$.http.method", is("GET"))
                .assertThat("$.http.uri", is("http://localhost/test?limit=1"))
                .assertThat("$.http.headers.*", hasSize(2))
                .assertThat("$.http.headers['Accept']", is(singletonList("application/json")))
                .assertThat("$.http.headers['Date']", is(singletonList("Tue, 15 Nov 1994 08:12:31 GMT")))
                .assertThat("$.http.body.person.name", is("Thomas"));
        }
        
        final HttpResponse response = MockHttpResponse.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(REMOTE)
                .withHeaders(MockHeaders.of("Date", "Tue, 15 Nov 1994 08:12:31 GMT"))
                .withContentType("application/json")
                .withBodyAsString("{\"person\":{\"name\":\"Magnus\"}}");

        sink.write(correlation, request, response);

        for(String last : new String[] {StaticAppender.getLastStatement(), PrettyPrintingStaticAppender.getLastStatement()} ) {
            with(last)
                .assertThat("$.http.origin", is("remote"))
                .assertThat("$.http.type", is("response"))
                .assertThat("$.http.correlation", is(correlationId))
                .assertThat("$.http.protocol", is("HTTP/1.0"))
                .assertThat("$.http.status", is(200))
                .assertThat("$.http.headers.*", hasSize(1))
                .assertThat("$.http.headers['Date']", is(singletonList("Tue, 15 Nov 1994 08:12:31 GMT")))
                .assertThat("$.http.body.person.name", is("Magnus"))
                .assertThat("$.http.duration", is(duration));
        }        
    }

}
