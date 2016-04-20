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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Multimap;
import com.google.gag.annotation.remark.Hack;
import com.google.gag.annotation.remark.OhNoYouDidnt;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

final class QueryParameters {

    public static String NONE = "org.zalando.logbook.QueryParameters.NONE";
    
    QueryParameters() {
        // package private so we can trick code coverage
    }

    static String render(final Multimap<String, String> parameters) {
        final Iterable<Map.Entry<String, String>> entries = parameters.entries();
        final StringBuilder queryString = new StringBuilder();
        final Iterator<Map.Entry<String, String>> parts = entries.iterator();

        if (parts.hasNext()) {
            appendTo(queryString, parts.next());
            while (parts.hasNext()) {
                queryString.append('&');
                appendTo(queryString, parts.next());
            }
        }
        
        return queryString.toString();
    }

    private static void appendTo(final StringBuilder queryString, final Map.Entry<String, String> entry) {
        queryString.append(urlEncodeUTF8(entry.getKey()));

        if (!Objects.equals(entry.getValue(), NONE)) {
            queryString.append('=');
            queryString.append(urlEncodeUTF8(entry.getValue()));
        }
    }

    private static String urlEncodeUTF8(final String s) {
        return urlEncode(s, "UTF-8");
    }

    @VisibleForTesting
    @Hack("Just so we can trick the code coverage")
    @OhNoYouDidnt
    static String urlEncode(final String s, final String charset) {
        try {
            return URLEncoder.encode(s, charset);
        } catch (final UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

}
