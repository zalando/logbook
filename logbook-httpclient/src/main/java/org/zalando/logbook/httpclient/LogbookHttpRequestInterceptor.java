package org.zalando.logbook.httpclient;

/*
 * #%L
 * Logbook: HTTP Client
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

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.zalando.logbook.Correlator;
import org.zalando.logbook.Logbook;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

public final class LogbookHttpRequestInterceptor implements HttpRequestInterceptor {

    private final Logbook logbook;
    private final Localhost localhost;

    public LogbookHttpRequestInterceptor(final Logbook logbook) {
        this(logbook, Localhost.resolve());
    }

    //@VisibleForTesting
    LogbookHttpRequestInterceptor(final Logbook logbook, final Localhost localhost) {
        this.logbook = logbook;
        this.localhost = localhost;
    }

    @Override
    public void process(final HttpRequest httpRequest, final HttpContext context) throws HttpException, IOException {
        final LocalRequest request = new LocalRequest(httpRequest, localhost);
        final Optional<Correlator> correlator = logbook.write(request);
        correlator.ifPresent(writeCorrelator(context));
    }

    private Consumer<Correlator> writeCorrelator(final HttpContext context) {
        return correlator -> context.setAttribute(Attributes.CORRELATOR, correlator);
    }

}
