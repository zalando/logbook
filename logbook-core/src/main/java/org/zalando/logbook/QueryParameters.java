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
import com.google.common.base.Splitter;
import com.google.common.collect.ForwardingMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.gag.annotation.remark.Hack;
import com.google.gag.annotation.remark.OhNoYouDidnt;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Multimaps.unmodifiableMultimap;

final class QueryParameters extends ForwardingMultimap<String, String> {

    private static final QueryParameters EMPTY = new QueryParameters();

    private final Multimap<String, String> parameters;

    private QueryParameters() {
        this(ImmutableMultimap.of());
    }

    QueryParameters(final Multimap<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    protected Multimap<String, String> delegate() {
        return parameters;
    }

    public QueryParameters obfuscate(final Obfuscator obfuscator) {
        return new QueryParameters(Multimaps.transformEntries(parameters, obfuscator::obfuscate));
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

    public static QueryParameters parse(@Nullable final String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            return EMPTY;
        }

        final List<String> entries = Splitter.on("&").splitToList(queryString);
        final Multimap<String, String> parameters = LinkedHashMultimap.create(entries.size(), 1);
        final Splitter splitter = Splitter.on('=');

        for (final String input : entries) {
            final Iterator<String> split = splitter.split(input).iterator();
            parameters.put(split.next(), split.hasNext() ? split.next() : null);
        }

        return new QueryParameters(unmodifiableMultimap(parameters));
    }

}
