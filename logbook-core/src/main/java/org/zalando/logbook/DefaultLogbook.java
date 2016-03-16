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

import com.google.common.annotations.VisibleForTesting;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

final class DefaultLogbook implements Logbook {

    private final HttpLogFormatter formatter;
    private final HttpLogWriter writer;
    private final Predicate<RawHttpRequest> predicate;
    private final Obfuscation obfuscation;

    DefaultLogbook(final HttpLogFormatter formatter, final HttpLogWriter writer, 
            final Predicate<RawHttpRequest> predicate, final Obfuscation obfuscation) {
        this.formatter = formatter;
        this.writer = writer;
        this.predicate = predicate;
        this.obfuscation = obfuscation;
    }

    @Override
    public Optional<Correlator> write(final RawHttpRequest rawHttpRequest) throws IOException {
        if (writer.isActive(rawHttpRequest) && predicate.test(rawHttpRequest)) {
            final String correlationId = UUID.randomUUID().toString();
            final HttpRequest request = obfuscation.obfuscate(rawHttpRequest.withBody());

            final Precorrelation<HttpRequest> precorrelation = new SimplePrecorrelation<>(correlationId, request);
            final String format = formatter.format(precorrelation);
            writer.writeRequest(new SimplePrecorrelation<>(correlationId, format));

            return Optional.of(rawHttpResponse -> {
                final HttpResponse response = obfuscation.obfuscate(rawHttpResponse.withBody());
                final Correlation<HttpRequest, HttpResponse> correlation =
                        new SimpleCorrelation<>(correlationId, request, response);
                final String message = formatter.format(correlation);
                writer.writeResponse(new SimpleCorrelation<>(correlationId, format, message));
            });
        } else {
            return Optional.empty();
        }
    }

    @VisibleForTesting
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

    @VisibleForTesting
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
