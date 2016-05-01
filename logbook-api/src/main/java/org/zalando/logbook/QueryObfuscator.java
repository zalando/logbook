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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static java.util.regex.Pattern.quote;

public interface QueryObfuscator {

    String obfuscate(final String query);

    static QueryObfuscator none() {
        return query -> query;
    }

    static QueryObfuscator obfuscate(final String name, final String replacement) {
        final Pattern pattern = Pattern.compile("((?:^|&)" + quote(name) + "=)(?:.*?)(&|$)");
        final String replacementPattern = "$1" + replacement + "$2";

        return query ->
                pattern.matcher(query).replaceAll(replacementPattern);
    }

    static QueryObfuscator compound(final QueryObfuscator... obfuscators) {
        return compound(asList(obfuscators));
    }

    static QueryObfuscator compound(final Iterable<QueryObfuscator> obfuscators) {
        return StreamSupport.stream(obfuscators.spliterator(), false)
                .reduce(none(), (left, right) ->
                        query -> left.obfuscate(right.obfuscate(query)));
    }

    static QueryObfuscator accessToken() {
        return obfuscate("access_token", "XXX");
    }

}
