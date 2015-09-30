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

import java.io.IOException;

public final class JsonHttpLogFormatter implements HttpLogFormatter {

    private final ObjectMapper mapper;

    public JsonHttpLogFormatter() {
        this(new ObjectMapper());
    }

    public JsonHttpLogFormatter(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String format(final String correlationId, final HttpRequest request) throws IOException {
        final ImmutableMap<String, Object> content = ImmutableMap.<String, Object>builder()
                .put("remote", request.getRemote())
                .put("method", request.getMethod())
                .put("uri", request.getRequestURI())
                .put("headers", request.getHeaders().asMap())
                .put("params", request.getParameters().asMap())
                .put("body", request.getBodyAsString())
                .build();

        return mapper.writeValueAsString(content);
    }

    @Override
    public String format(final String correlationId, final HttpResponse response) throws IOException {
        final ImmutableMap<String, Object> content = ImmutableMap.<String, Object>builder()
                .put("status", response.getStatus())
                .put("headers", response.getHeaders().asMap())
                .put("body", response.getBodyAsString())
                .build();

        return mapper.writeValueAsString(content);
    }

}
