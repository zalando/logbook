package org.zalando.logbook.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.CorrelationId;
import org.zalando.logbook.attributes.AttributeExtractor;
import org.zalando.logbook.attributes.HttpAttributes;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.RequestFilter;
import org.zalando.logbook.ResponseFilter;
import org.zalando.logbook.Sink;
import org.zalando.logbook.Strategy;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Predicate;

import static lombok.AccessLevel.PRIVATE;

@AllArgsConstructor
@Slf4j
final class DefaultLogbook implements Logbook {

    private final Predicate<HttpRequest> predicate;
    private final CorrelationId correlationId;
    private final RequestFilter requestFilter;
    private final ResponseFilter responseFilter;
    private final Strategy strategy;
    private final AttributeExtractor attributeExtractor;
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

            final HttpAttributes requestAttributes = extractAttributesOrEmpty(processedRequest);
            final HttpRequest request = new CachingHttpRequest(processedRequest, requestAttributes);
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
                        final HttpAttributes responseAttributes = extractAttributesOrEmpty(processedRequest, processedResponse);
                        final HttpResponse response = new CachingHttpResponse(processedResponse, responseAttributes);
                        final HttpResponse filteredResponse = responseFilter.filter(response);
                        strategy.write(precorrelation.correlate(), filteredRequest, filteredResponse, sink);
                    };
                }
            };
        } else {
            return Stages.noop();
        }
    }

    private HttpAttributes extractAttributesOrEmpty(final HttpRequest request) {
        try {
            return attributeExtractor.extract(request);
        } catch (Exception e) {
            log.trace("AttributeExtractor throw exception while processing request: `{}`", e.getMessage());
            return HttpAttributes.EMPTY;
        }
    }

    private HttpAttributes extractAttributesOrEmpty(final HttpRequest request, final HttpResponse response) {
        try {
            return attributeExtractor.extract(request, response);
        } catch (Exception e) {
            log.trace("AttributeExtractor throw exception while processing response: `{}`", e.getMessage());
            return HttpAttributes.EMPTY;
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
