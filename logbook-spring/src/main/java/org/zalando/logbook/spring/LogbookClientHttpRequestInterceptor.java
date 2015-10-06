package org.zalando.logbook.spring;

/*
 * #%L
 * Logbook: Spring
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
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.zalando.logbook.Correlator;
import org.zalando.logbook.Logbook;

import java.io.IOException;
import java.util.Optional;

public final class LogbookClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final Logbook logbook;
    private final Localhost localhost;

    public LogbookClientHttpRequestInterceptor(final Logbook logbook) {
        this(logbook, Localhost.resolve());
    }

    @VisibleForTesting
    LogbookClientHttpRequestInterceptor(final Logbook logbook, final Localhost localhost) {
        this.logbook = logbook;
        this.localhost = localhost;
    }

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
            final ClientHttpRequestExecution execution) throws IOException {

        final Optional<Correlator> correlator = logbook.write(new Request(request, body, localhost));
        final ClientHttpResponse original = execution.execute(request, body);

        if (correlator.isPresent()) {
            final Response response = new Response(original);
            correlator.get().write(response);
            return response.asClientHttpResponse();
        } else {
            return original;
        }
    }

}
