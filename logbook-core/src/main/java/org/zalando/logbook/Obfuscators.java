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

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.quote;
import static java.util.stream.Collectors.toList;

public final class Obfuscators {

    Obfuscators() {
        // package private so we can trick code coverage
    }

    public static QueryObfuscator obfuscate(final String name, final String replacement) {
        final Pattern pattern = Pattern.compile("((?:^|&)" + quote(name) + "=)(?:.*?)(&|$)");
        final String replacementPattern = "$1" + replacement + "$2";

        return query -> pattern.matcher(query).replaceAll(replacementPattern);
    }

    public static QueryObfuscator accessToken() {
        return obfuscate("access_token", "XXX");
    }

    public static HeaderObfuscator obfuscate(final Predicate<String> keyPredicate, final String replacement) {
        return (key, value) -> keyPredicate.test(key) ? replacement : value;
    }

    public static HeaderObfuscator obfuscate(final BiPredicate<String, String> predicate, final String replacement) {
        return (key, value) -> predicate.test(key, value) ? replacement : value;
    }

    public static HeaderObfuscator authorization() {
        return obfuscate("Authorization"::equalsIgnoreCase, "XXX");
    }

    static Map<String, List<String>> obfuscateHeaders(final Map<String, List<String>> map, final BiFunction<String, String, String> f) {
        final BaseHttpMessage.HeadersBuilder builder = new BaseHttpMessage.HeadersBuilder();
        for (final Map.Entry<String, List<String>> e : map.entrySet()) {
            final String k = e.getKey();
            builder.put(k, e.getValue().stream().map(x -> f.apply(k, x)).collect(toList()));
        }
        return builder.build();
    }
}
