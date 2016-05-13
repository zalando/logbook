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

import java.nio.charset.Charset;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

public interface BaseHttpMessage {

    String getProtocolVersion();

    Origin getOrigin();

    Map<String, List<String>> getHeaders();

    String getContentType();

    Charset getCharset();

    class HeadersBuilder {

        private final Map<String, List<String>> headers;

        public HeadersBuilder() {
            // package private so we can trick code coverage
            headers = new TreeMap<>(CASE_INSENSITIVE_ORDER);
        }

        public HeadersBuilder put(final String key, final String value) {
            final List<String> values = headers.get(key);
            if (values != null) {
                values.add(value);
            } else {
                final ArrayList<String> list = new ArrayList<>();
                list.add(value);
                headers.put(key, list);
            }
            return this;
        }

        public HeadersBuilder put(final String key, final Iterable<String> values) {
            for (String value : values) {
                put(key, value);
            }
            return this;
        }

        public Map<String, List<String>> build() {
            return Collections.unmodifiableMap(headers);
        }
    }
}
