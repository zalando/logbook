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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

public final class JsonHttpLogFormatter implements HttpLogFormatter {

    private final ObjectMapper mapper;

    public JsonHttpLogFormatter() {
        this(new ObjectMapper());
    }

    public JsonHttpLogFormatter(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String format(final Precorrelation precorrelation) throws IOException {
        final String correlationId = precorrelation.getId();
        final HttpRequest request = precorrelation.getRequest();

        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.<String, Object>builder();

        builder.put("correlation", correlationId);
        builder.put("remote", request.getRemote());
        builder.put("method", request.getMethod());
        builder.put("uri", request.getRequestURI());

        addUnless(builder, "headers", request.getHeaders().asMap(), Map::isEmpty);
        addUnless(builder, "params", request.getParameters().asMap(), Map::isEmpty);
        addUnless(builder, "body", request.getBodyAsString(), String::isEmpty);

        final ImmutableMap<String, Object> content = builder.build();

        return mapper.writeValueAsString(content);
    }

    @Override
    public String format(final Correlation correlation) throws IOException {
        final String correlationId = correlation.getId();
        final HttpResponse response = correlation.getResponse();

        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.<String, Object>builder();

        builder.put("correlation", correlationId);
        builder.put("status", response.getStatus());
        addUnless(builder, "headers", response.getHeaders().asMap(), Map::isEmpty);
        addUnless(builder, "body", response.getBodyAsString(), String::isEmpty);

        final ImmutableMap<String, Object> content = builder.build();

        return mapper.writeValueAsString(content);
    }

    private static <T> void addUnless(final ImmutableMap.Builder<String, Object> target, final String key,
            final T element, final Predicate<T> predicate) {

        if (!predicate.test(element)) {
            target.put(key, element);
        }
    }

}
