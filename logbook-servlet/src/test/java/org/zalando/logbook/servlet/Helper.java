package org.zalando.logbook.servlet;

/*
 * #%L
 * Logbook: Servlet
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

import org.mockito.stubbing.Answer;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpMessage;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;
import java.util.function.Consumer;

import static org.mockito.Matchers.any;

public final class Helper {

    @FunctionalInterface
    interface ThrowingConsumer<T> {

        void accept(T t) throws Exception;
    }
    @SuppressWarnings("unchecked")
    public static Precorrelation<HttpRequest> anyPrecorrelation() {
        return any(Precorrelation.class);
    }

    @SuppressWarnings("unchecked")
    public static Correlation<HttpRequest, HttpResponse> anyCorrelation() {
        return any(Correlation.class);
    }

    public static Answer<?> validateRequest(final Consumer<HttpRequest> validator) throws IOException {
        return invocation -> {
            @SuppressWarnings("unchecked")
            final Precorrelation<HttpRequest> precorrelation = (Precorrelation) invocation.getArguments()[0];
            validator.accept(precorrelation.getRequest());
            return invocation.callRealMethod();
        };
    }

    public static Answer<?> validateResponse(final ThrowingConsumer<HttpResponse> validator) throws IOException {
        return invocation -> {
            @SuppressWarnings("unchecked")
            final Correlation<HttpRequest, HttpResponse> precorrelation = (Correlation) invocation.getArguments()[0];
            validator.accept(precorrelation.getResponse());
            return invocation.callRealMethod();
        };
    }

    static String getBodyAsString(final HttpMessage message) {
        try {
            return message.getBodyAsString();
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

}
