package org.zalando.logbook;

/*
 * #%L
 * Logbook: API
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

import com.google.gag.annotation.remark.Hack;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

public interface Logbook {

    Optional<Correlator> write(final RawHttpRequest request) throws IOException;

    static Logbook create() {
        return builder().build();
    }

    static Logbook create(
            @Nullable final Predicate<RawHttpRequest> predicate,
            @Nullable final HttpLogFormatter formatter,
            @Nullable final HttpLogWriter writer,
            @Nullable final HeaderObfuscator headerObfuscator,
            @Nullable final QueryObfuscator queryObfuscator,
            @Nullable final BodyObfuscator bodyObfuscator) {
        return Creator.create(predicate, formatter, writer, headerObfuscator, queryObfuscator, bodyObfuscator);
    }

    static Creator.Builder builder() {
        return Creator.builder();
    }

    @Hack("The Lombok IDEA plugin doesn't like @Builder on static interface methods")
    final class Creator {

        @lombok.Builder(builderClassName = "Builder")
        static Logbook create(
                @Nullable final Predicate<RawHttpRequest> predicate,
                @Nullable final HttpLogFormatter formatter,
                @Nullable final HttpLogWriter writer,
                @Nullable final HeaderObfuscator headerObfuscator,
                @Nullable final QueryObfuscator queryObfuscator,
                @Nullable final BodyObfuscator bodyObfuscator) {

            final LogbookFactory factory = LogbookFactory.INSTANCE;
            return factory.create(predicate, headerObfuscator, queryObfuscator, bodyObfuscator, formatter, writer);
        }

    }
}
