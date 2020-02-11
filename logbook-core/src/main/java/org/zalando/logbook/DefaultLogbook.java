package org.zalando.logbook;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Predicate;

import static lombok.AccessLevel.PRIVATE;

@AllArgsConstructor
final class DefaultLogbook implements Logbook {

    private final Predicate<HttpRequest> predicate;
    private final CorrelationId correlationId;
    private final RequestFilter requestFilter;
    private final ResponseFilter responseFilter;
    private final Strategy strategy;
    private final Sink sink;
    private final Clock clock = Clock.systemUTC();

    @Override
    public RequestWritingStage process(final HttpRequest originalRequest) throws IOException {
        return process(originalRequest, strategy);
    }

    @Override
    public RequestWritingStage process(final HttpRequest originalRequest, final Strategy strategy) throws IOException {

        if (sink.isActive() && predicate.test(originalRequest)) {
            final Precorrelation precorrelation = newPrecorrelation(originalRequest);
            final HttpRequest processedRequest = strategy.process(originalRequest);

            final HttpRequest request = new CachingHttpRequest(processedRequest);
            final HttpRequest filteredRequest = requestFilter.filter(request);

            return new RequestWritingStage() {
                @Override
                public ResponseProcessingStage write() throws IOException {
                    strategy.write(precorrelation, filteredRequest, sink);

                    return this;
                }

                @Override
                public ResponseWritingStage process(final HttpResponse originalResponse) throws IOException {
                    final HttpResponse processedResponse = strategy.process(filteredRequest, originalResponse);

                    return () -> {
                        final HttpResponse response = new CachingHttpResponse(processedResponse);
                        final HttpResponse filteredResponse = responseFilter.filter(response);
                        strategy.write(precorrelation.correlate(), filteredRequest, filteredResponse, sink);
                    };
                }
            };
        } else {
            return Stages.noop();
        }
    }

    private Precorrelation newPrecorrelation(final HttpRequest request) {
        return new SimplePrecorrelation(correlationId.generate(request), clock);
    }

    static final class SimplePrecorrelation implements Precorrelation {

        @Getter
        private final String id;

        private final Clock clock;

        @Getter
        private final Instant start;

        // visible for testing
        SimplePrecorrelation(final String id, final Clock clock) {
            this.id = id;
            this.clock = clock;
            this.start = Instant.now(clock);
        }

        @Override
        public Correlation correlate() {
            return new SimpleCorrelation(id, start, Instant.now(clock));
        }

    }

    @AllArgsConstructor(access = PRIVATE)
    @Getter
    static final class SimpleCorrelation implements Correlation {

        private final String id;
        private final Instant start;
        private final Instant end;
        private final Duration duration;

        SimpleCorrelation(final String id, final Instant start, final Instant end) {
            this(id, start, end, Duration.between(start, end));
        }

    }

}
