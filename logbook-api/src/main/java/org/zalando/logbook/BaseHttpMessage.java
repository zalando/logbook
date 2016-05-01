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

import com.google.common.collect.ListMultimap;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.TreeMap;

import static com.google.common.collect.Multimaps.newListMultimap;
import static java.lang.String.CASE_INSENSITIVE_ORDER;

public interface BaseHttpMessage {

    String getProtocolVersion();

    Origin getOrigin();

    ListMultimap<String, String> getHeaders();

    String getContentType();

    Charset getCharset();

    class Headers {

        Headers() {
            // package private so we can trick code coverage
        }

        public static ListMultimap<String, String> create() {
            return newListMultimap(new TreeMap<>(CASE_INSENSITIVE_ORDER), ArrayList::new);
        }

    }

}
