package org.zalando.logbook.undertow;

/*
 * #%L
 * Logbook: Undertow
 * %%
 * Copyright (C) 2015 - 2016 Zalando SE
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

import static java.util.Objects.requireNonNull;

import java.io.IOException;

import java.util.Optional;
import java.util.function.Consumer;

import org.zalando.logbook.Correlator;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.RawHttpRequest;
import org.zalando.logbook.RawHttpResponse;

import com.google.common.annotations.VisibleForTesting;

import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;

import io.undertow.util.AttachmentKey;

public class LogbookHandler implements HttpHandler, ExchangeCompletionListener {

    private final Logbook logbook;
    private final Consumer<Throwable> writeFailureHandler;

    private final AttachmentKey<Correlator> correlatorKey = AttachmentKey.create(Correlator.class);

    private volatile HttpHandler next = ResponseCodeHandler.HANDLE_404;

    public LogbookHandler(final Logbook logbook, final Consumer<Throwable> writeFailureHandler) {
        this.logbook = requireNonNull(logbook);
        this.writeFailureHandler = requireNonNull(writeFailureHandler);
    }

    public void setNext(final HttpHandler next) {
        this.next = requireNonNull(next);
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        logRequest(exchange);
        next.handleRequest(exchange);
    }

    private void logRequest(final HttpServerExchange exchange) {
        logRequest(new UndertowHttpRequest(exchange)).ifPresent(correlator -> {
            storeCorrelator(exchange, correlator);
            exchange.addExchangeCompleteListener(this);
        });
    }

    @VisibleForTesting
    void storeCorrelator(final HttpServerExchange exchange, final Correlator correlator) {
        exchange.putAttachment(correlatorKey, correlator);
    }

    private Optional<Correlator> logRequest(final RawHttpRequest request) {
        try {
            return logbook.write(request);
        } catch (final IOException e) {
            writeFailureHandler.accept(e);
            return Optional.empty();
        }
    }

    @Override
    public void exchangeEvent(final HttpServerExchange exchange, final NextListener nextListener) {
        try {
            final Correlator correlator = exchange.removeAttachment(correlatorKey);
            if (correlator != null) {
                logResponse(correlator, new UntertowHttpResponse(exchange));
            }
        } finally {
            nextListener.proceed();
        }
    }

    private void logResponse(final Correlator correlator, final RawHttpResponse response) {
        try {
            correlator.write(response);
        } catch (final IOException e) {
            writeFailureHandler.accept(e);
        }
    }
}
