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

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

public final class JsonHttpLogFormatter implements HttpLogFormatter {

    private static final MediaType APPLICATION_JSON = MediaType.create("application", "json");
    private static final CharMatcher PRETTY_PRINT = CharMatcher.anyOf("\n\t");
    private static final Logger LOG = LoggerFactory.getLogger(JsonHttpLogFormatter.class);

    private final ObjectMapper mapper;

    public JsonHttpLogFormatter() {
        this(new ObjectMapper());
    }

    public JsonHttpLogFormatter(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String format(final Precorrelation<HttpRequest> precorrelation) throws IOException {
        final String correlationId = precorrelation.getId();
        final HttpRequest request = precorrelation.getRequest();

        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.<String, Object>builder();

        builder.put("origin", translate(request.getOrigin()));
        builder.put("type", "request");
        builder.put("correlation", correlationId);
        builder.put("protocol", request.getProtocolVersion());
        builder.put("remote", request.getRemote());
        builder.put("method", request.getMethod());
        builder.put("uri", request.getRequestUri());

        addUnless(builder, "headers", request.getHeaders().asMap(), Map::isEmpty);
        addBody(request, builder);

        final ImmutableMap<String, Object> content = builder.build();

        return mapper.writeValueAsString(content);
    }

    @Override
    public String format(final Correlation<HttpRequest, HttpResponse> correlation) throws IOException {
        final String correlationId = correlation.getId();
        final HttpResponse response = correlation.getResponse();

        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.<String, Object>builder();

        builder.put("origin", translate(response.getOrigin()));
        builder.put("type", "response");
        builder.put("correlation", correlationId);
        builder.put("protocol", response.getProtocolVersion());
        builder.put("status", response.getStatus());
        addUnless(builder, "headers", response.getHeaders().asMap(), Map::isEmpty);
        addBody(response, builder);

        final ImmutableMap<String, Object> content = builder.build();

        return mapper.writeValueAsString(content);
    }

    private static String translate(final Origin origin) {
        return origin.name().toLowerCase(Locale.ROOT);
    }

    private static <T> void addUnless(final ImmutableMap.Builder<String, Object> target, final String key,
                                      final T element, final Predicate<T> predicate) {

        if (!predicate.test(element)) {
            target.put(key, element);
        }
    }

    private void addBody(final HttpMessage request, final ImmutableMap.Builder<String, Object> builder) throws IOException {
        final String body = request.getBodyAsString();

        if (isJson(request.getContentType())) {
            builder.put("body", tryParseBodyAsJson(body));
        } else {
            addUnless(builder, "body", body, String::isEmpty);
        }
    }

    private Object tryParseBodyAsJson(final String body) {
        if (body.isEmpty()) {
            return JsonBody.EMPTY;
        }

        try {
            return new JsonBody(compactJson(body));
        } catch (final IOException e) {
            LOG.trace("Unable to parse body as JSON", e);
        }

        return body;
    }

    private boolean isJson(final String contentType) {
        if (contentType.isEmpty()) {
            return false;
        }

        final MediaType mediaType = MediaType.parse(contentType);

        final boolean isJson = mediaType.is(APPLICATION_JSON);

        final boolean isApplication = mediaType.is(MediaType.ANY_APPLICATION_TYPE);
        final boolean isCustomJson = mediaType.subtype().endsWith("+json");

        return isJson || isApplication && isCustomJson;
    }

    private String compactJson(final String json) throws IOException {
        if (isAlreadyCompacted(json)) {
            return json;
        }

        final StringWriter output = new StringWriter();
        final JsonFactory factory = mapper.getFactory();
        final JsonParser parser = factory.createParser(json);

        final JsonGenerator generator = factory.createGenerator(output);

        // see http://stackoverflow.com/questions/17354150/8-branches-for-try-with-resources-jacoco-coverage-possible
        //noinspection TryFinallyCanBeTryWithResources - jacoco can't handle try-with correctly
        try {
            while (parser.nextToken() != null) {
                generator.copyCurrentEvent(parser);
            }
        } finally {
            generator.close();
        }

        return output.toString();
    }

    // this wouldn't catch spaces in json, but that's ok for our use case here
    private boolean isAlreadyCompacted(final String json) {
        return PRETTY_PRINT.matchesNoneOf(json);
    }

    private static final class JsonBody {

        static final String EMPTY = "";

        private final String json;

        private JsonBody(final String json) {
            this.json = json;
        }

        @JsonRawValue
        @JsonValue
        public String getJson() {
            return json;
        }

    }

}
