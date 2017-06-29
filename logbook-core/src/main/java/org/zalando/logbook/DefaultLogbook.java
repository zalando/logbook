package org.zalando.logbook;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

final class DefaultLogbook implements Logbook {

    private final Predicate<RawHttpRequest> predicate;
    private final RawRequestFilter rawRequestFilter;
    private final RawResponseFilter rawResponseFilter;
    private final RequestFilter requestFilter;
    private final ResponseFilter responseFilter;
    private final HttpLogFormatter formatter;
    private final HttpLogWriter writer;
    private final Clock clock = Clock.systemUTC();

    DefaultLogbook(
            final Predicate<RawHttpRequest> predicate,
            final RawRequestFilter rawRequestFilter,
            final RawResponseFilter rawResponseFilter,
            final RequestFilter requestFilter,
            final ResponseFilter responseFilter,
            final HttpLogFormatter formatter,
            final HttpLogWriter writer) {
        this.predicate = predicate;
        this.rawRequestFilter = rawRequestFilter;
        this.rawResponseFilter = rawResponseFilter;
        this.requestFilter = requestFilter;
        this.responseFilter = responseFilter;
        this.formatter = formatter;
        this.writer = writer;
    }

    @Override
    public Optional<Correlator> write(final RawHttpRequest rawHttpRequest) throws IOException {
        final Instant start = Instant.now(clock);
        if (writer.isActive(rawHttpRequest) && predicate.test(rawHttpRequest)) {
            final String correlationId = UUID.randomUUID().toString();
            final RawHttpRequest filteredRawHttpRequest = rawRequestFilter.filter(rawHttpRequest);
            final HttpRequest request = requestFilter.filter(filteredRawHttpRequest.withBody());

            final Precorrelation<HttpRequest> precorrelation = new SimplePrecorrelation<>(correlationId, request);
            final String format = formatter.format(precorrelation);
            writer.writeRequest(new SimplePrecorrelation<>(correlationId, format));

            return Optional.of(rawHttpResponse -> {
                final Instant end = Instant.now(clock);
                final Duration duration = Duration.between(start, end);
                final RawHttpResponse filteredRawHttpResponse = rawResponseFilter.filter(rawHttpResponse);
                final HttpResponse response = responseFilter.filter(filteredRawHttpResponse.withBody());
                final Correlation<HttpRequest, HttpResponse> correlation =
                        new SimpleCorrelation<>(correlationId, duration, request, response);
                final String message = formatter.format(correlation);
                writer.writeResponse(new SimpleCorrelation<>(correlationId, duration, format, message));
            });
        } else {
            return Optional.empty();
        }
    }

    static class SimplePrecorrelation<I> implements Precorrelation<I> {

        private final String id;
        private final I request;

        public SimplePrecorrelation(final String id, final I request) {
            this.id = id;
            this.request = request;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public I getRequest() {
            return request;
        }

    }

    static class SimpleCorrelation<I, O> implements Correlation<I, O> {

        private final String id;
        private final Duration duration;
        private final I request;
        private final O response;

        public SimpleCorrelation(final String id, final Duration duration, final I request, final O response) {
            this.id = id;
            this.duration = duration;
            this.request = request;
            this.response = response;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public Duration getDuration() {
            return duration;
        }

        @Override
        public I getRequest() {
            return request;
        }

        @Override
        public O getResponse() {
            return response;
        }

    }

}
