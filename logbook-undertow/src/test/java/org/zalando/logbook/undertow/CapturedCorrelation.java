package org.zalando.logbook.undertow;

/*
 * #%L
 * Logbook: Undertow
 * %%
 * Copyright (C) 2016 Zalando SE
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

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.zalando.logbook.Correlation;
import org.zalando.logbook.Correlator;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.RawHttpResponse;

public final class CapturedCorrelation
        implements Precorrelation<HttpRequest>, Correlation<HttpRequest, HttpResponse>, Correlator {

    private final HttpRequest request;
    private final AtomicReference<HttpResponse> response = new AtomicReference<>();

    CapturedCorrelation(final HttpRequest request) {
        this.request = requireNonNull(request);
    }

    @Override
    public String getId() {
        return Integer.toString(hashCode());
    }

    @Override
    public HttpRequest getRequest() {
        return request;
    }

    @Override
    public void write(final RawHttpResponse response) throws IOException {
        checkState(this.response.compareAndSet(null, response.withBody()), "Response has already been captured!");
    }

    public boolean isResponseCaptured() {
        return response.get() != null;
    }

    @Override
    public HttpResponse getResponse() {
        final HttpResponse theResponse = response.get();
        checkState(theResponse != null, "No response has been captured so far!");
        return theResponse;
    }
}
