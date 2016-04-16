package org.zalando.logbook;

/*
 * #%L
 * Logbook: Core
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

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

public interface Logbook {

    Optional<Correlator> write(final RawHttpRequest request) throws IOException;

    static Logbook create() {
        return builder().build();
    }

    @lombok.Builder(builderClassName = "Builder")
    static Logbook create(
            @Nullable final HttpLogFormatter formatter,
            @Nullable final HttpLogWriter writer,
            @Nullable final Predicate<RawHttpRequest> predicate,
            @Nullable final Obfuscator headerObfuscator,
            @Nullable final Obfuscator parameterObfuscator,
            @Nullable final BodyObfuscator bodyObfuscator
    ) {

        return new DefaultLogbook(
                Util.firstNonNull(formatter, new DefaultHttpLogFormatter()),
                Util.firstNonNull(writer, new DefaultHttpLogWriter()),
                Util.firstNonNull(predicate, request -> true),
                new Obfuscation(
                        Util.firstNonNull(headerObfuscator, Obfuscator.none()),
                        Util.firstNonNull(parameterObfuscator, Obfuscator.none()),
                        Util.firstNonNull(bodyObfuscator, BodyObfuscator.none())));
    }

}
