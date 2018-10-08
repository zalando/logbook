package org.zalando.logbook;

import org.apiguardian.api.API;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
public interface PreparedHttpLogFormatter extends HttpLogFormatter {

    @Override
    default String format(final Precorrelation<HttpRequest> precorrelation) throws IOException {
        return format(prepare(precorrelation));
    }

    @Override
    default String format(final Correlation<HttpRequest, HttpResponse> correlation) throws IOException {
        return format(prepare(correlation));
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
    String format(Map<String, Object> content) throws IOException;

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
    default Map<String, Object> prepare(final Precorrelation<HttpRequest> precorrelation) throws IOException {
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
    default Map<String, Object> prepare(final Correlation<HttpRequest, HttpResponse> correlation) throws IOException {
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


    default void addBody(final HttpMessage message, final Map<String, Object> content) throws IOException {
        addUnless(content, "body", message.getBodyAsString(), String::isEmpty);
    }

    static <T> void addUnless(
            final Map<String, Object> content,
            final String key,
            final T element,
            final Predicate<T> predicate
    ) {
        if (!predicate.test(element)) {
            content.put(key, element);
        }
    }

}
