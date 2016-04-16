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

//import com.google.common.annotations.VisibleForTesting;
//import com.google.common.base.Splitter;

import com.google.gag.annotation.remark.Hack;
import com.google.gag.annotation.remark.OhNoYouDidnt;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

final class QueryParameters extends Multimaps.BasicMultimap<String, String> implements Multimap<String, String> { //implements Multimap<String, String>  {

    private static final QueryParameters EMPTY = new QueryParameters();

    private QueryParameters() {
        super();
    }

    private QueryParameters(Multimap<String, String> aValue) {
        super();
        this.putAll(aValue);
    }

    public QueryParameters obfuscate(final Obfuscator obfuscator) {
        return new QueryParameters(Multimaps.transformEntries(this, obfuscator::obfuscate));
    }

    @Override
    public String toString() {
        return join(entries());
    }

    private String join(final Iterable<Map.Entry<String, String>> entries) {
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

    private void appendTo(final StringBuilder queryString, final Map.Entry<String, String> e) {
        queryString.append(urlEncodeUTF8(e.getKey()));

        if (e.getValue() != null) {
            queryString.append('=');
            queryString.append(urlEncodeUTF8(e.getValue()));
        }
    }
//    }

    private static String urlEncodeUTF8(final String s) {
        return urlEncode(s, "UTF-8");
    }

    //    @VisibleForTesting
    @Hack("Just so we can trick the code coverage")
    @OhNoYouDidnt
    static String urlEncode(final String s, final String charset) {
        try {
            return URLEncoder.encode(s, charset);
        } catch (final UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    public static QueryParameters parse(@Nullable final String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            return EMPTY;
        }

        final List<String> entries = Arrays.asList(queryString.split("&"));//Splitter.on("&").splitToList(queryString);
        final QueryParameters parameters = new QueryParameters();

        for (final String input : entries) {
            final String[] split = input.split("=");
            parameters.putValue(split[0], split.length > 1 ? split[1] : input.endsWith("=") ? "" : null);
        }

        return parameters;
    }

}
