package org.zalando.logbook;

/*
 * #%L
 * Logbook
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

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;

@FunctionalInterface
public interface Obfuscator {

    String obfuscate(final String key, final String value);

    static Obfuscator none() {
        return (key, value) -> value;
    }

    static Obfuscator obfuscate(final Predicate<String> keyPredicate, final String replacement) {
        return (key, value) -> keyPredicate.test(key) ? replacement : value;
    }

    static Obfuscator obfuscate(final BiPredicate<String, String> predicate, final String replacement) {
        return (key, value) -> predicate.test(key, value) ? replacement : value;
    }

    static Obfuscator compound(final Obfuscator... obfuscators) {
        return compound(asList(obfuscators));
    }

    static Obfuscator compound(final Iterable<Obfuscator> obfuscators) {
        return StreamSupport.stream(obfuscators.spliterator(), false)
                .reduce(none(), (left, right) ->
                        (key, value) ->
                                left.obfuscate(key, right.obfuscate(key, value)));
    }

    static Obfuscator authorization() {
        return obfuscate("Authorization"::equalsIgnoreCase, "XXX");
    }

}
