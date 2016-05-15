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
import java.util.Optional;
import java.util.function.Predicate;


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


        final HeaderObfuscator header = Optional.ofNullable(headerObfuscator).orElseGet(Obfuscators::authorization);
        final BodyObfuscator body = Optional.ofNullable(bodyObfuscator).orElseGet(BodyObfuscator::none);

        return new DefaultLogbook(
                Optional.ofNullable(condition).orElse($ -> true),
                combine(queryObfuscator, header, body, requestObfuscator),
                combine(header, body, responseObfuscator),
                Optional.ofNullable(formatter).orElseGet(DefaultHttpLogFormatter::new),
                Optional.ofNullable(writer).orElseGet(DefaultHttpLogWriter::new)
        );
    }

    @Nonnull
    private RequestObfuscator combine(
            @Nullable final QueryObfuscator queryObfuscator,
            final HeaderObfuscator headerObfuscator,
            final BodyObfuscator bodyObfuscator,
            @Nullable final RequestObfuscator requestObfuscator) {

        final QueryObfuscator query = Optional.ofNullable(queryObfuscator).orElseGet(Obfuscators::accessToken);

        return RequestObfuscator.merge(
                Optional.ofNullable(requestObfuscator).orElseGet(RequestObfuscator::none),
                request -> new ObfuscatedHttpRequest(request, query, headerObfuscator, bodyObfuscator));
    }

    @Nonnull
    private ResponseObfuscator combine(
            final HeaderObfuscator headerObfuscator,
            final BodyObfuscator bodyObfuscator,
            @Nullable final ResponseObfuscator responseObfuscator) {

        return ResponseObfuscator.merge(
                Optional.ofNullable(responseObfuscator).orElseGet(ResponseObfuscator::none),
                response -> new ObfuscatedHttpResponse(response, headerObfuscator, bodyObfuscator));
    }
}
