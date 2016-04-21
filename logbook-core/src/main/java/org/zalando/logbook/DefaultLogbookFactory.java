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

import javax.annotation.Nullable;
import java.util.function.Predicate;

import static com.google.common.base.MoreObjects.firstNonNull;

public final class DefaultLogbookFactory implements LogbookFactory {

    @Override
    public Logbook create(
            @Nullable Predicate<RawHttpRequest> predicate, 
            @Nullable Obfuscator headerObfuscator, 
            @Nullable Obfuscator parameterObfuscator, 
            @Nullable BodyObfuscator bodyObfuscator, 
            @Nullable HttpLogFormatter formatter,
            @Nullable HttpLogWriter writer) {

        return new DefaultLogbook(
                firstNonNull(predicate, request -> true), 
                new Obfuscation(
                        firstNonNull(headerObfuscator, Obfuscator.authorization()),
                        firstNonNull(parameterObfuscator, Obfuscator.none()),
                        firstNonNull(bodyObfuscator, BodyObfuscator.none())), 
                firstNonNull(formatter, new DefaultHttpLogFormatter()),
                firstNonNull(writer, new DefaultHttpLogWriter())
        );
    }

}
