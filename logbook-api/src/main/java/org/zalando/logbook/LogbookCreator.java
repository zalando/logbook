package org.zalando.logbook;

/*
 * #%L
 * Logbook: API
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

import lombok.Singular;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public final class LogbookCreator {

    LogbookCreator() {
        // package private so we can trick code coverage
    }

    @lombok.Builder(builderClassName = "Builder")
    private static Logbook create(
            @Nullable final Predicate<RawHttpRequest> condition,
            @Singular final List<QueryObfuscator> queryObfuscators,
            @Singular final List<HeaderObfuscator> headerObfuscators,
            @Singular final List<BodyObfuscator> bodyObfuscators,
            @Singular final List<RequestObfuscator> requestObfuscators,
            @Singular final List<ResponseObfuscator> responseObfuscators,
            @Nullable final HttpLogFormatter formatter,
            @Nullable final HttpLogWriter writer) {

        final LogbookFactory factory = LogbookFactory.INSTANCE;

        final QueryObfuscator queryObfuscator = queryObfuscators.stream()
                .reduce(QueryObfuscator::merge)
                .orElse(null);

        final HeaderObfuscator headerObfuscator = headerObfuscators.stream()
                .reduce(HeaderObfuscator::merge)
                .orElse(null);

        final BodyObfuscator bodyObfuscator = bodyObfuscators.stream()
                .reduce(BodyObfuscator::merge)
                .orElse(null);

        final RequestObfuscator requestObfuscator = requestObfuscators.stream()
                .reduce(RequestObfuscator::merge)
                .orElse(null);

        final ResponseObfuscator responseObfuscator = responseObfuscators.stream()
                .reduce(ResponseObfuscator::merge)
                .orElse(null);

        return factory.create(
                condition,
                queryObfuscator,
                headerObfuscator,
                bodyObfuscator,
                requestObfuscator,
                responseObfuscator,
                formatter,
                writer);
    }

}
