package org.zalando.logbook;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.STABLE;

/**
 * A custom {@link HttpLogFormatter} that produces JSON objects. It can be augmented with composition:
 *
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
@API(status = STABLE)
public final class JsonHttpLogFormatter implements HttpLogFormatter {

    private static final Logger LOG = LoggerFactory.getLogger(JsonHttpLogFormatter.class);
    private static final Predicate<String> JSON = MediaTypeQuery.compile("application/json", "application/*+json");

    private final ObjectMapper mapper;
    private final JsonHeuristic heuristic = new JsonHeuristic();
    private final JsonCompactor compactor;

    public JsonHttpLogFormatter() {
        this(new ObjectMapper());
    }

    public JsonHttpLogFormatter(final ObjectMapper mapper) {
        this.mapper = mapper;
        this.compactor = new JsonCompactor(mapper);
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
     * @throws IOException if reading body fails
     * @see #prepare(Correlation)
     * @see #format(Map)
     * @see DefaultHttpLogFormatter#prepare(Precorrelation)
     */
    @API(status = EXPERIMENTAL)
    public Map<String, Object> prepare(final Precorrelation<HttpRequest> precorrelation) throws IOException {
        final String correlationId = precorrelation.getId();
        final HttpRequest request = precorrelation.getRequest();

        final Map<String, Object> content = new LinkedHashMap<>();

        content.put("origin", Origins.translate(request.getOrigin()));
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
     * @throws IOException if reading body fails
     * @see #prepare(Correlation)
     * @see #format(Map)
     * @see DefaultHttpLogFormatter#prepare(Correlation)
     */
    @API(status = EXPERIMENTAL)
    public Map<String, Object> prepare(final Correlation<HttpRequest, HttpResponse> correlation) throws IOException {
        final HttpResponse response = correlation.getResponse();

        final Map<String, Object> content = new LinkedHashMap<>();

        content.put("origin", Origins.translate(response.getOrigin()));
        content.put("type", "response");
        content.put("correlation", correlation.getId());
        content.put("duration", correlation.getDuration().toMillis());
        content.put("protocol", response.getProtocolVersion());
        content.put("status", response.getStatus());

        addUnless(content, "headers", response.getHeaders(), Map::isEmpty);
        addBody(response, content);

        return content;
    }

    private static <T> void addUnless(final Map<String, Object> target, final String key,
            final T element, final Predicate<T> predicate) {

        if (!predicate.test(element)) {
            target.put(key, element);
        }
    }

    private void addBody(final HttpMessage message, final Map<String, Object> map) throws IOException {
        final String body = message.getBodyAsString();

        if (isContentTypeJson(message)) {
            map.put("body", tryParseBodyAsJson(body));
        } else {
            addUnless(map, "body", body, String::isEmpty);
        }
    }

    private boolean isContentTypeJson(final HttpMessage message) {
        return JSON.test(message.getContentType());
    }

    private Object tryParseBodyAsJson(final String body) {
        if (heuristic.isProbablyJson(body)) {
            if (compactor.isCompacted(body)) {
                // any body that looks like JSON (according to our heuristic) and has no newlines would be
                // incorrectly treated as JSON here which results in an invalid JSON output
                // see https://github.com/zalando/logbook/issues/279
                return new JsonBody(body);
            }

            try {
                return new JsonBody(compactor.compact(body));
            } catch (final IOException e) {
                LOG.trace("Unable to compact body, probably because it's not JSON. Embedding it as-is: [{}]", e.getMessage());
                return body;
            }
        } else {
            return body;
        }
    }

    @AllArgsConstructor
    private static final class JsonBody {
        String json;

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
     * @throws IOException if writing JSON output fails
     * @see #prepare(Precorrelation)
     * @see #prepare(Correlation)
     * @see DefaultHttpLogFormatter#format(List)
     */
    @API(status = EXPERIMENTAL)
    public String format(final Map<String, Object> content) throws IOException {
        return mapper.writeValueAsString(content);
    }

}
