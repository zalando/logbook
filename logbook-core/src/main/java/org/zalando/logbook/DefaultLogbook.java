package org.zalando.logbook;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import static lombok.AccessLevel.PRIVATE;

@AllArgsConstructor
final class DefaultLogbook implements Logbook {

    private final Predicate<HttpRequest> predicate;
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
        final HttpRequest request = new CachingHttpRequest(originalRequest);

        if (sink.isActive() && predicate.test(request)) {
            final Precorrelation precorrelation = new SimplePrecorrelation(clock);
            final HttpRequest filteredRequest = requestFilter.filter(request);
            final HttpRequest processedRequest = strategy.process(filteredRequest);

            return () -> {
                strategy.write(precorrelation, processedRequest, sink);

                return originalResponse -> {
                    final HttpResponse response = new CachingHttpResponse(originalResponse);
                    final HttpResponse filteredResponse = responseFilter.filter(response);
                    final HttpResponse processedResponse = strategy.process(processedRequest, filteredResponse);

                    return () ->
                            strategy.write(precorrelation.correlate(), processedRequest, processedResponse, sink);
                };
            };
        } else {
            return Stages.noop();
        }
    }

    static final class SimplePrecorrelation implements Precorrelation {

        @Getter
        private final String id;

        private final Clock clock;

        @Getter
        private final Instant start;

        SimplePrecorrelation(final Clock clock) {
            this(generateCorrelationId(), clock);
        }

        // visible for testing
        SimplePrecorrelation(final String id, final Clock clock) {
            this.id = id;
            this.clock = clock;
            this.start = Instant.now(clock);
        }

        // TODO interface?
        private static String generateCorrelationId() {
            // set most significant bit to produce fixed length string
            return Long.toHexString(ThreadLocalRandom.current().nextLong() | Long.MIN_VALUE);
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
