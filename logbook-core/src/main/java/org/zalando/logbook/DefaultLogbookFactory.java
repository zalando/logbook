package org.zalando.logbook;

/*
 * #%L
 * Logbook: Core
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

import static com.google.common.base.MoreObjects.firstNonNull;

public final class DefaultLogbookFactory implements LogbookFactory {

    @Override
    public Logbook create(
            @Nullable final Predicate<RawHttpRequest> condition,
            @Nullable final QueryObfuscator queryObfuscator,
            @Nullable final HeaderObfuscator headerObfuscator,
            @Nullable final BodyObfuscator bodyObfuscator,
            @Nullable final RequestObfuscator requestObfuscator,
            @Nullable final ResponseObfuscator responseObfuscator,
            @Nullable final HttpLogFormatter formatter,
            @Nullable final HttpLogWriter writer) {

        return new DefaultLogbook(
                firstNonNull(condition, $ -> true),
                combine(queryObfuscator, headerObfuscator, bodyObfuscator, requestObfuscator),
                combine(headerObfuscator, bodyObfuscator, responseObfuscator),
                firstNonNull(formatter, new DefaultHttpLogFormatter()),
                firstNonNull(writer, new DefaultHttpLogWriter())
        );
    }

    @Nonnull
    private RequestObfuscator combine(
            @Nullable final QueryObfuscator queryObfuscator,
            @Nullable final HeaderObfuscator headerObfuscator,
            @Nullable final BodyObfuscator bodyObfuscator,
            @Nullable final RequestObfuscator requestObfuscator) {

        final QueryObfuscator query = firstNonNull(queryObfuscator, QueryObfuscator.none());
        final HeaderObfuscator header = firstNonNull(headerObfuscator, HeaderObfuscator.none());
        final BodyObfuscator body = firstNonNull(bodyObfuscator, BodyObfuscator.none());

        return RequestObfuscator.merge(
                firstNonNull(requestObfuscator, RequestObfuscator.none()),
                request -> new ObfuscatedHttpRequest(request, query, header, body));
    }

    @Nonnull
    private ResponseObfuscator combine(
            @Nullable final HeaderObfuscator headerObfuscator,
            @Nullable final BodyObfuscator bodyObfuscator,
            @Nullable final ResponseObfuscator responseObfuscator) {


        final HeaderObfuscator header = firstNonNull(headerObfuscator, HeaderObfuscator.none());
        final BodyObfuscator body = firstNonNull(bodyObfuscator, BodyObfuscator.none());

        return ResponseObfuscator.merge(
                firstNonNull(responseObfuscator, ResponseObfuscator.none()),
                response -> new ObfuscatedHttpResponse(response, header, body));
    }

}
