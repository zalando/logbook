package org.zalando.logbook.benchmark;

import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.zalando.logbook.api.Correlation;
import org.zalando.logbook.api.HttpHeaders;
import org.zalando.logbook.api.HttpRequest;
import org.zalando.logbook.api.HttpResponse;
import org.zalando.logbook.api.Precorrelation;
import org.zalando.logbook.benchmark.jmh.DefaultCorrelation;
import org.zalando.logbook.benchmark.jmh.DefaultPrecorrelation;
import org.zalando.logbook.test.MockHttpRequest;
import org.zalando.logbook.test.MockHttpResponse;

import java.time.Duration;
import java.time.Instant;

@State(Scope.Benchmark)
public class RequestResponseState {

    protected HttpResponse response;
    protected HttpRequest request;

    private final Instant now = Instant.now();
    protected DefaultCorrelation defaultCorrelation = new DefaultCorrelation(
            "id", now, now.plusMillis(100), Duration.ofMillis(100));
    protected DefaultPrecorrelation defaultPrecorrelation = new DefaultPrecorrelation("id", defaultCorrelation);

    protected HttpResponse minimalResponse;
    protected Correlation correlation;

    protected HttpRequest minimalRequest;
    protected Precorrelation precorrelation;

    @Setup(Level.Trial)
    public void setUp(final HeaderState headerState) throws Exception {
        minimalResponse = MockHttpResponse.create()
                .withContentType("application/json")
                .withHeaders(HttpHeaders.of("Content-Type", "application/json"))
                .withBodyAsString("{\"name\":\"Bob\"}");

        minimalRequest = MockHttpRequest.create()
                .withContentType("application/json")
                .withHeaders(HttpHeaders.of("Content-Type", "application/json"))
                .withBodyAsString("{\"name\":\"Bob\"}");

        request = MockHttpRequest.create()
                .withContentType("application/json")
                .withHeaders(headerState.getAllRequestHeaders())
                .withBodyAsString("{\"name\":\"Bob\"}");

        response = MockHttpResponse.create()
                .withContentType("application/json")
                .withHeaders(headerState.getAllResponseHeaders())
                .withBodyAsString("{\"name\":\"Bob\"}");
    }

    public HttpRequest getRequest() {
        return request;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public DefaultCorrelation getDefaultCorrelation() {
        return defaultCorrelation;
    }

    public DefaultPrecorrelation getDefaultPrecorrelation() {
        return defaultPrecorrelation;
    }

    public HttpRequest getMinimalRequest() {
        return minimalRequest;
    }

    public HttpResponse getMinimalResponse() {
        return minimalResponse;
    }

    public Correlation getCorrelation() {
        return correlation;
    }

    public Precorrelation getPrecorrelation() {
        return precorrelation;
    }
}
