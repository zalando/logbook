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
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

final class QueryParameters implements Multimap<String, String>  {

    private static final QueryParameters EMPTY = new QueryParameters();

    private final Multimap<String, String> parameters;

    private QueryParameters() {
        this(Multimaps.immutableOf());
    }

    QueryParameters(final Multimap<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public Set<Entry<String, String>> entries() {
        return parameters.entries();
    }

    @Override
    public int size() {
        return parameters.size();
    }

    @Override
    public boolean isEmpty() {
        return parameters.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return parameters.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return parameters.containsValue(value);
    }

    @Override
    public Collection<String> get(final Object key) {
        return parameters.get(key);
    }

    @Override
    public Collection<String> put(final String key, final Collection<String> value) {
        return parameters.put(key, value);
    }

    @Override
    public Collection<String> remove(final Object key) {
        return parameters.remove(key);
    }

    @Override
    public void putAll(final Map<? extends String, ? extends Collection<String>> m) {
        parameters.putAll(m);
    }

    @Override
    public void clear() {
        parameters.clear();
    }

    @Override
    public Set<String> keySet() {
        return parameters.keySet();
    }

    @Override
    public Collection<Collection<String>> values() {
        return parameters.values();
    }

    @Override
    public Set<Entry<String, Collection<String>>> entrySet() {
        return parameters.entrySet();
    }

    @Override
    public boolean equals(final Object o) {
        return parameters.equals(o);
    }

    @Override
    public int hashCode() {
        return parameters.hashCode();
    }

    @Override
    public Collection<String> getOrDefault(final Object key, final Collection<String> defaultValue) {
        return parameters.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(final BiConsumer<? super String, ? super Collection<String>> action) {
        parameters.forEach(action);
    }

    @Override
    public void replaceAll(
            final BiFunction<? super String, ? super Collection<String>, ? extends Collection<String>> function
    ) {
        parameters.replaceAll(function);
    }

    @Override
    public Collection<String> putIfAbsent(final String key, final Collection<String> value) {
        return parameters.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        return parameters.remove(key, value);
    }

    @Override
    public boolean replace(final String key, final Collection<String> oldValue, final Collection<String> newValue) {
        return parameters.replace(key, oldValue, newValue);
    }

    @Override
    public Collection<String> replace(final String key, final Collection<String> value) {
        return parameters.replace(key, value);
    }

    @Override
    public Collection<String> computeIfAbsent(
            final String key, final Function<? super String, ? extends Collection<String>> mappingFunction
    ) {
        return parameters.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public Collection<String> computeIfPresent(
            final String key,
            final BiFunction<? super String, ? super Collection<String>, ? extends Collection<String>> remappingFunction
    ) {
        return parameters.computeIfPresent(key, remappingFunction);
    }

    @Override
    public Collection<String> compute(
            final String key,
            final BiFunction<? super String, ? super Collection<String>, ? extends Collection<String>> remappingFunction
    ) {
        return parameters.compute(key, remappingFunction);
    }

    @Override
    public Collection<String> merge(
            final String key, final Collection<String> value,
            final BiFunction<? super Collection<String>, ? super Collection<String>, ? extends Collection<String>> remappingFunction
    ) {
        return parameters.merge(key, value, remappingFunction);
    }

    public QueryParameters obfuscate(final Obfuscator obfuscator) {
        return new QueryParameters(Multimaps.transformEntries(parameters, obfuscator::obfuscate));
    }

    @Override
    public String toString() {
        return join(parameters.entries());
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
        final Multimap<String, String> parameters = Multimaps.immutableOf();

        for (final String input : entries) {
            final String[] split = input.split("=");
            parameters.putValue(split[0], split.length > 1 ? split[1] : input.endsWith("=") ? "" : null);
        }

        return new QueryParameters(parameters);
    }

}
