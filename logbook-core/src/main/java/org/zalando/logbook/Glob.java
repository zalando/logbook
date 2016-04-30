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

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Glob {

    private static final Pattern GLOB_PATTERN = Pattern.compile("\\?|(/\\*{2}$)|\\*{2}|(/\\*$)|\\*");

    public static Predicate<String> compile(final String glob) {
        final StringBuilder result = new StringBuilder();
        final Matcher matcher = GLOB_PATTERN.matcher(glob);

        int end = 0;

        if (matcher.find()) {
            do {
                result.append(quote(glob, end, matcher.start()));

                final String match = matcher.group();

                if ("?".equals(match)) {
                    result.append('.');
                } else if ("/**".equals(match)) {
                    result.append("(/.*)?$");
                } else if ("**".equals(match)) {
                    result.append(".*?");
                } else if ("/*".equals(match)) {
                    result.append("/[^/]*$");
                } else {
                    result.append("[^/]*?");
                }

                end = matcher.end();
            } while (matcher.find());
        } else {
            return glob::equals;
        }

        result.append(quote(glob, end, glob.length()));
        final Pattern pattern = Pattern.compile(result.toString());
        return path -> pattern.matcher(path).matches();
    }

    private static String quote(final String s, final int start, final int end) {
        if (start == end) {
            return "";
        }
        return Pattern.quote(s.substring(start, end));
    }

}
