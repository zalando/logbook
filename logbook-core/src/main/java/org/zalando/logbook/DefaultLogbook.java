package org.zalando.logbook;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

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
        if (sink.isActive() && predicate.test(originalRequest)) {
            final Precorrelation precorrelation = new SimplePrecorrelation(clock);
            final HttpRequest processedRequest = strategy.process(originalRequest);

            return () -> {
                final HttpRequest filteredRequest = requestFilter.filter(processedRequest);
                strategy.write(precorrelation, filteredRequest, sink);
                return originalResponse -> {
                    final HttpResponse processedResponse = strategy.process(originalRequest, originalResponse);
                    return () -> {
                        final HttpResponse filteredResponse = responseFilter.filter(processedResponse);
                        strategy.write(precorrelation.correlate(), filteredRequest, filteredResponse, sink);
                    };
                };
            };
        } else {
            return () -> response -> () -> {
                // nothing to do
            };
        }
    }

    static class SimplePrecorrelation implements Precorrelation {

        private final String id;
        private final Clock clock;
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
