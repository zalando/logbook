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

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

final class Mockbook implements Logbook {

    private final Predicate<RawHttpRequest> predicate;
    private final QueryObfuscator queryObfuscator;
    private final HeaderObfuscator headerObfuscator;
    private final BodyObfuscator bodyObfuscator;
    private final RequestObfuscator requestObfuscator;
    private final ResponseObfuscator responseObfuscator;
    private final HttpLogFormatter formatter;
    private final HttpLogWriter writer;

    public Mockbook(
            @Nullable final Predicate<RawHttpRequest> predicate,
            @Nullable final QueryObfuscator queryObfuscator,
            @Nullable final HeaderObfuscator headerObfuscator,
            @Nullable final BodyObfuscator bodyObfuscator,
            @Nullable final RequestObfuscator requestObfuscator,
            @Nullable final ResponseObfuscator responseObfuscator,
            @Nullable final HttpLogFormatter formatter,
            @Nullable final HttpLogWriter writer) {
        this.predicate = predicate;
        this.queryObfuscator = queryObfuscator;
        this.headerObfuscator = headerObfuscator;
        this.bodyObfuscator = bodyObfuscator;
        this.requestObfuscator = requestObfuscator;
        this.responseObfuscator = responseObfuscator;
        this.formatter = formatter;
        this.writer = writer;
    }

    @Override
    public Optional<Correlator> write(final RawHttpRequest request) throws IOException {
        throw new UnsupportedOperationException();
    }

    public Predicate<RawHttpRequest> getPredicate() {
        return predicate;
    }

    public BodyObfuscator getBodyObfuscator() {
        return bodyObfuscator;
    }

    public HeaderObfuscator getHeaderObfuscator() {
        return headerObfuscator;
    }

    public QueryObfuscator getQueryObfuscator() {
        return queryObfuscator;
    }

    public RequestObfuscator getRequestObfuscator() {
        return requestObfuscator;
    }

    public ResponseObfuscator getResponseObfuscator() {
        return responseObfuscator;
    }

    public HttpLogFormatter getFormatter() {
        return formatter;
    }

    public HttpLogWriter getWriter() {
        return writer;
    }

}
