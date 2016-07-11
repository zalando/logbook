package org.zalando.logbook;

/*
 * #%L
 * Logbook: Core
 * %%
 * Copyright (C) 2015 Zalando SE
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

final class DefaultLogbook implements Logbook {

    private final Predicate<RawHttpRequest> predicate;
    private final RequestObfuscator requestObfuscator;
    private final ResponseObfuscator responseObfuscator;
    private final HttpLogFormatter formatter;
    private final HttpLogWriter writer;

    DefaultLogbook(
            final Predicate<RawHttpRequest> predicate,
            final RequestObfuscator requestObfuscator,
            final ResponseObfuscator responseObfuscator,
            final HttpLogFormatter formatter,
            final HttpLogWriter writer) {
        this.predicate = predicate;
        this.requestObfuscator = requestObfuscator;
        this.responseObfuscator = responseObfuscator;
        this.formatter = formatter;
        this.writer = writer;
    }

    @Override
    public Optional<Correlator> write(final RawHttpRequest rawHttpRequest) throws IOException {
        final long start = System.nanoTime();
        if (writer.isActive(rawHttpRequest) && predicate.test(rawHttpRequest)) {
            final String correlationId = UUID.randomUUID().toString();
            final HttpRequest request = requestObfuscator.obfuscate(rawHttpRequest.withBody());

            final Precorrelation<HttpRequest> precorrelation = new SimplePrecorrelation<>(correlationId, request);
            final String format = formatter.format(precorrelation);
            writer.writeRequest(new SimplePrecorrelation<>(correlationId, format));

            return Optional.of(rawHttpResponse -> {
                final HttpResponse response = responseObfuscator.obfuscate(rawHttpResponse.withBody());
                final Duration elapsedTime = Duration.ofNanos(System.nanoTime() - start);

                final Correlation<HttpRequest, HttpResponse> correlation =
                        new SimpleCorrelation<>(correlationId, request, response, elapsedTime);
                final String message = formatter.format(correlation);
                writer.writeResponse(new SimpleCorrelation<>(correlationId, format, message, elapsedTime));
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
        private final Duration elapsedTime;

        public SimpleCorrelation(final String id, final I request, final O response, final Duration elapsedTime) {
            this.id = id;
            this.request = request;
            this.response = response;
            this.elapsedTime = elapsedTime;
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

        @Override
        public Duration getElapsedTime() {
            return elapsedTime;
        }
    }
}
