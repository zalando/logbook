package org.zalando.logbook;

import com.google.common.annotations.VisibleForTesting;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

final class DefaultLogbook implements Logbook {

    private final HttpLogFormatter formatter;
    private final HttpLogWriter writer;
    private final Obfuscation obfuscation;

    DefaultLogbook(final HttpLogFormatter formatter, final HttpLogWriter writer, final Obfuscation obfuscation) {
        this.formatter = formatter;
        this.writer = writer;
        this.obfuscation = obfuscation;
    }

    @Override
    public Optional<Correlator> write(final RawHttpRequest rawHttpRequest) throws IOException {
        if (writer.isActive(rawHttpRequest)) {
            final String correlationId = UUID.randomUUID().toString();
            final HttpRequest request = obfuscation.obfuscate(rawHttpRequest.withBody());

            writer.writeRequest(formatter.format(new SimplePrecorrelation(correlationId, request)));

            return Optional.of(rawHttpResponse -> {
                final HttpResponse response = obfuscation.obfuscate(rawHttpResponse.withBody());
                final String message = formatter.format(new SimpleCorrelation(correlationId, request, response));
                writer.writeResponse(message);
            });
        } else {
            return Optional.empty();
        }
    }

    @VisibleForTesting
    static class SimplePrecorrelation implements Precorrelation {

        private final String id;
        private final HttpRequest request;

        public SimplePrecorrelation(final String id, final HttpRequest request) {
            this.id = id;
            this.request = request;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public HttpRequest getRequest() {
            return request;
        }

    }

    @VisibleForTesting
    static class SimpleCorrelation implements Correlation {

        private final String id;
        private final HttpRequest request;
        private final HttpResponse response;

        public SimpleCorrelation(final String id, final HttpRequest request, final HttpResponse response) {
            this.id = id;
            this.request = request;
            this.response = response;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public HttpRequest getRequest() {
            return request;
        }

        @Override
        public HttpResponse getResponse() {
            return response;
        }

    }

}
