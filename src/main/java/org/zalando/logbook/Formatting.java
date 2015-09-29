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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.UnmodifiableIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.common.collect.Iterators.addAll;
import static com.google.common.collect.Iterators.forEnumeration;

// TODO sort headers by name?!
final class Formatting {

    static Multimap<String, String> getHeaders(final HttpServletRequest request) {
        final Multimap<String, String> headers = ArrayListMultimap.create();
        final UnmodifiableIterator<String> iterator = forEnumeration(request.getHeaderNames());

        while (iterator.hasNext()) {
            final String header = iterator.next();
            addAll(headers.get(header), forEnumeration(request.getHeaders(header)));
        }

        return headers;
    }


    static Multimap<String, String> getHeaders(final HttpServletResponse response) {
        final Multimap<String, String> headers = ArrayListMultimap.create();

        for (final String header : response.getHeaderNames()) {
            headers.putAll(header, response.getHeaders(header));
        }

        return headers;
    }

}
