package org.zalando.logbook;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class JsonHttpLogFormatter implements HttpLogFormatter {

    private static final Logger LOG = LoggerFactory.getLogger(JsonHttpLogFormatter.class);
    private static final Pattern JSON = Pattern.compile("application/(?:json|[^\t ;]+\\+json)(?:$|\t| |;)");

    private final ObjectMapper mapper;

    public JsonHttpLogFormatter() {
        this(new ObjectMapper());
    }

    public JsonHttpLogFormatter(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String format(final Precorrelation<HttpRequest> precorrelation) throws IOException {
        return format(prepare(precorrelation));
    }

    public Map<String, Object> prepare(final Precorrelation<HttpRequest> precorrelation) throws IOException {
        final String correlationId = precorrelation.getId();
        final HttpRequest request = precorrelation.getRequest();

        final Map<String, Object> content = new LinkedHashMap<>();

        content.put("origin", translate(request.getOrigin()));
        content.put("type", "request");
        content.put("correlation", correlationId);
        content.put("protocol", request.getProtocolVersion());
        content.put("remote", request.getRemote());
        content.put("method", request.getMethod());
        content.put("uri", request.getRequestUri());

        addUnless(content, "headers", request.getHeaders(), Map::isEmpty);
        addBody(request, content);

        return content;
    }

    @Override
    public String format(final Correlation<HttpRequest, HttpResponse> correlation) throws IOException {
        return format(prepare(correlation));
    }

    public Map<String, Object> prepare(final Correlation<HttpRequest, HttpResponse> correlation) throws IOException {
        final String correlationId = correlation.getId();
        final HttpResponse response = correlation.getResponse();

        final Map<String, Object> content = new LinkedHashMap<>();

        content.put("origin", translate(response.getOrigin()));
        content.put("type", "response");
        content.put("correlation", correlationId);
        content.put("protocol", response.getProtocolVersion());
        content.put("status", response.getStatus());

        addUnless(content, "headers", response.getHeaders(), Map::isEmpty);
        addBody(response, content);

        return content;
    }

    private static String translate(final Origin origin) {
        return origin.name().toLowerCase(Locale.ROOT);
    }

    private static <T> void addUnless(final Map<String, Object> target, final String key,
                                      final T element, final Predicate<T> predicate) {

        if (!predicate.test(element)) {
            target.put(key, element);
        }
    }

    private void addBody(final HttpMessage message, final Map<String, Object> map) throws IOException {
        final String body = message.getBodyAsString();

        if (isJson(message.getContentType())) {
            map.put("body", tryParseBodyAsJson(body));
        } else {
            addUnless(map, "body", body, String::isEmpty);
        }
    }

    private Object tryParseBodyAsJson(final String body) {
        if (body.isEmpty()) {
            return JsonBody.EMPTY;
        }

        try {
            return new JsonBody(compactJson(body));
        } catch (final IOException e) {
            LOG.trace("Unable to parse body as JSON; embedding it as-is", e);
        }

        return body;
    }

    private boolean isJson(final String type) {
        return !type.isEmpty() && JSON.matcher(type).lookingAt();

    }

    private String compactJson(final String json) throws IOException {
        if (isAlreadyCompacted(json)) {
            return json;
        }

        final StringWriter output = new StringWriter();
        final JsonFactory factory = mapper.getFactory();
        final JsonParser parser = factory.createParser(json);

        final JsonGenerator generator = factory.createGenerator(output);

        // https://github.com/jacoco/jacoco/wiki/FilteringOptions
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
        return json.indexOf('\n') == -1;
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

    public String format(final Map<String, Object> content) throws IOException {
        return mapper.writeValueAsString(content);
    }

}
