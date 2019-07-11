package org.zalando.logbook;

import org.apiguardian.api.API;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
public interface StructuredHttpLogFormatter extends HttpLogFormatter {

    @Override
    default String format(final Precorrelation precorrelation, final HttpRequest request) throws IOException {
        return format(prepare(precorrelation, request));
    }

    @Override
    default String format(final Correlation correlation, final HttpResponse response)
            throws IOException {
        return format(prepare(correlation, response));
    }

    /**
     * Renders properties of an HTTP message into a JSON string.
     *
     * @param content individual parts of an HTTP message
     * @return the whole message as a JSON object
     * @throws IOException if writing JSON output fails
     * @see #prepare(Precorrelation, HttpRequest)
     * @see #prepare(Correlation, HttpResponse)
     */
    String format(Map<String, Object> content) throws IOException;

    /**
     * Produces a map of individual properties from an HTTP request.
     *
     * @param precorrelation the correlation
     * @param request the request
     * @return a map containing HTTP request attributes
     * @throws IOException if reading body fails
     * @see #prepare(Correlation, HttpResponse)
     * @see #format(Map)
     */
    default Map<String, Object> prepare(final Precorrelation precorrelation, final HttpRequest request)
            throws IOException {
        final String correlationId = precorrelation.getId();

        final Map<String, Object> content = new LinkedHashMap<>();

        content.put("origin", request.getOrigin().name().toLowerCase(Locale.ROOT));
        content.put("type", "request");
        content.put("correlation", correlationId);
        content.put("protocol", request.getProtocolVersion());
        content.put("remote", request.getRemote());
        content.put("method", request.getMethod());
        content.put("uri", request.getRequestUri());

        prepareHeaders(request).ifPresent(headers -> content.put("headers", headers));
        prepareBody(request).ifPresent(body -> content.put("body", body));

        return content;
    }

    /**
     * Produces a map of individual properties from an HTTP response.
     *
     * @param correlation the correlation
     * @param response the response
     * @return a map containing HTTP response attributes
     * @throws IOException if reading body fails
     * @see #prepare(Correlation, HttpResponse)
     * @see #format(Map)
     */
    default Map<String, Object> prepare(final Correlation correlation, final HttpResponse response) throws IOException {
        final Map<String, Object> content = new LinkedHashMap<>();

        content.put("origin", response.getOrigin().name().toLowerCase(Locale.ROOT));
        content.put("type", "response");
        content.put("correlation", correlation.getId());
        content.put("duration", correlation.getDuration().toMillis());
        content.put("protocol", response.getProtocolVersion());
        content.put("status", response.getStatus());

        prepareHeaders(response).ifPresent(headers -> content.put("headers", headers));
        prepareBody(response).ifPresent(body -> content.put("body", body));

        return content;
    }

    default Optional<Map<String, List<String>>> prepareHeaders(final HttpMessage message) {
        final Map<String, List<String>> headers = message.getHeaders();
        return Optional.ofNullable(headers.isEmpty() ? null : headers);
    }

    default Optional<Object> prepareBody(final HttpMessage message) throws IOException {
        final String body = message.getBodyAsString();
        return Optional.ofNullable(body.isEmpty() ? null : body);
    }

}
