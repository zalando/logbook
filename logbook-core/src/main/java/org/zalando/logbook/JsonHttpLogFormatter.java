package org.zalando.logbook;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A custom {@link HttpLogFormatter} that produces JSON objects. It can be augmented with composition:
 * <p>
 * <pre>
 * {@code
 *
 * public class CustomsFormatter implements HttpLogFormatter {
 *
 *     private final JsonHttpLogFormatter delegate;
 *
 *     public CustomsFormatter(final ObjectMapper mapper) {
 *         this.delegate = new JsonHttpLogFormatter(mapper);
 *     }
 *
 *     public String format(final Precorrelation<HttpRequest> precorrelation) throws IOException {
 *         Map<String, Object> request = delegate.prepare(precorrelation);
 *         // TODO modify request here
 *         return delegate.format(request);
 *     }
 *
 *     public String format(final Correlation<HttpRequest, HttpResponse> correlation) throws IOException {
 *         Map<String, Object> response = delegate.prepare(correlation);
 *         // TODO modify response here
 *         return delegate.format(response);
 *      }
 *
 * }
 * }
 * </pre>
 */
public final class JsonHttpLogFormatter implements HttpLogFormatter {

    private static final Logger LOG = LoggerFactory.getLogger(JsonHttpLogFormatter.class);
    private static final Predicate<String> JSON = MediaTypeQuery.compile("application/json", "application/*+json");

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

    /**
     * Produces a map of individual properties from an HTTP request.
     *
     * @param precorrelation the request correlation
     * @return a map containing HTTP request attributes
     * @throws IOException
     * @see #prepare(Correlation)
     * @see #format(Map)
     * @see DefaultHttpLogFormatter#prepare(Precorrelation)
     */
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

    /**
     * Produces a map of individual properties from an HTTP response.
     *
     * @param correlation the response correlation
     * @return a map containing HTTP response attributes
     * @throws IOException
     * @see #prepare(Correlation)
     * @see #format(Map)
     * @see DefaultHttpLogFormatter#prepare(Correlation)
     */
    public Map<String, Object> prepare(final Correlation<HttpRequest, HttpResponse> correlation) throws IOException {
        final HttpResponse response = correlation.getResponse();

        final Map<String, Object> content = new LinkedHashMap<>();

        content.put("origin", translate(response.getOrigin()));
        content.put("type", "response");
        content.put("correlation", correlation.getId());
        content.put("duration", correlation.getDuration().toMillis());
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
            LOG.trace("Unable to parse body as JSON; embedding it as-is: [{}]", e.getMessage());
            return body;
        }
    }

    private boolean isJson(@Nullable final String type) {
        return JSON.test(type);
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

    /**
     * Renders properties of an HTTP message into a JSON string.
     *
     * @param content individual parts of an HTTP message
     * @return the whole message as a JSON object
     * @throws IOException
     * @see #prepare(Precorrelation)
     * @see #prepare(Correlation)
     * @see DefaultHttpLogFormatter#format(List)
     */
    public String format(final Map<String, Object> content) throws IOException {
        return mapper.writeValueAsString(content);
    }

}
