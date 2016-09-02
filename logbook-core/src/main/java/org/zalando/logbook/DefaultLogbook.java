package org.zalando.logbook;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

final class DefaultLogbook implements Logbook {

    private final Predicate<RawHttpRequest> predicate;
    private final RequestFilter requestFilter;
    private final ResponseFilter responseFilter;
    private final HttpLogFormatter formatter;
    private final HttpLogWriter writer;

    DefaultLogbook(
            final Predicate<RawHttpRequest> predicate,
            final RequestFilter requestFilter,
            final ResponseFilter responseFilter,
            final HttpLogFormatter formatter,
            final HttpLogWriter writer) {
        this.predicate = predicate;
        this.requestFilter = requestFilter;
        this.responseFilter = responseFilter;
        this.formatter = formatter;
        this.writer = writer;
    }

    @Override
    public Optional<Correlator> write(final RawHttpRequest rawHttpRequest) throws IOException {
        if (writer.isActive(rawHttpRequest) && predicate.test(rawHttpRequest)) {
            final String correlationId = UUID.randomUUID().toString();
            final HttpRequest request = requestFilter.filter(rawHttpRequest.withBody());

            final Precorrelation<HttpRequest> precorrelation = new SimplePrecorrelation<>(correlationId, request);
            final String format = formatter.format(precorrelation);
            writer.writeRequest(new SimplePrecorrelation<>(correlationId, format));

            return Optional.of(rawHttpResponse -> {
                final HttpResponse response = responseFilter.filter(rawHttpResponse.withBody());
                final Correlation<HttpRequest, HttpResponse> correlation =
                        new SimpleCorrelation<>(correlationId, request, response);
                final String message = formatter.format(correlation);
                writer.writeResponse(new SimpleCorrelation<>(correlationId, format, message));
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
        private final I request;
        private final O response;

        public SimpleCorrelation(final String id, final I request, final O response) {
            this.id = id;
            this.request = request;
            this.response = response;
        }

        @Override
        public String getId() {
            return id;
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
