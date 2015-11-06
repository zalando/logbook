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
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.*;
import com.google.gag.annotation.remark.Hack;
import com.google.gag.annotation.remark.OhNoYouDidnt;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.CharMatcher.anyOf;
import static com.google.common.collect.Multimaps.unmodifiableMultimap;
import static java.util.stream.Collectors.toList;

final class QueryParameters extends ForwardingMultimap<String, String> {

    private static final QueryParameters EMPTY = new QueryParameters();

    private final Multimap<String, String> parameters;

    public QueryParameters() {
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
        return Joiner.on("&").withKeyValueSeparator("=").useForNull("")
                .join(parameters.entries().stream().map(QueryParameters::encodeEntry).collect(toList()));
    }

    private static Map.Entry<String, String> encodeEntry(final Map.Entry<String, String> entry) {
        return Maps.immutableEntry(urlEncodeUTF8(entry.getKey()), urlEncodeUTF8(entry.getValue()));
    }

    private static String urlEncodeUTF8(@Nullable final String s) {
        return urlEncodeUTF8(s, "UTF-8");
    }

    @VisibleForTesting
    @Hack("Just so we can trick the code coverage")
    @OhNoYouDidnt
    static String urlEncodeUTF8(@Nullable final String s, final String charset) {
        if (s == null) {
            return null;
        }

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

        final List<Map.Entry<String, String>> entries = splitEntries(queryString);
        final int size = entries.size();
        final Multimap<String, String> parameters = LinkedHashMultimap.create(size, 1);

        for (final Map.Entry<String, String> entry : entries) {
            parameters.put(entry.getKey(), entry.getValue());
        }

        return new QueryParameters(unmodifiableMultimap(parameters));
    }

    private static List<Map.Entry<String, String>> splitEntries(final String queryString) {
        final List<String> entries = Splitter.on(anyOf("&;")).splitToList(queryString);

        return entries.stream().map(QueryParameters::parseEntry).collect(toList());
    }

    private static Map.Entry<String, String> parseEntry(final String input) {
        if (input.contains("=")) {
            final Iterator<String> split = Splitter.on('=').split(input).iterator();
            return Maps.immutableEntry(split.next(), split.next());
        } else {
            return Maps.immutableEntry(input, null);
        }
    }

}
