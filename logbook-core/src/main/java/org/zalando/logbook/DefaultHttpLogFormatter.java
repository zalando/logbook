package org.zalando.logbook;

import org.apiguardian.api.API;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.STABLE;
import static org.zalando.logbook.Origin.REMOTE;

@API(status = STABLE)
public final class DefaultHttpLogFormatter implements HttpLogFormatter {

    @Override
    public String format(final Precorrelation precorrelation, final HttpRequest request) throws IOException {
        return format(prepare(precorrelation, request));
    }

    /**
     * Produces an HTTP-like request in individual lines.
     *
     * @param precorrelation the request correlation
     * @return a line-separated HTTP request
     * @throws IOException if reading body fails
     * @see #prepare(Correlation, HttpResponse)
     * @see #format(List)
     * @see StructuredHttpLogFormatter#prepare(Precorrelation, HttpRequest)
     */
    @API(status = EXPERIMENTAL)
    public List<String> prepare(final Precorrelation precorrelation, final HttpRequest request) throws IOException {
        final String requestLine = String.format("%s %s %s", request.getMethod(), request.getRequestUri(),
                request.getProtocolVersion());
        return prepare(request, "Request", precorrelation.getId(), requestLine);
    }

    @Override
    public String format(final Correlation correlation, final HttpResponse response) throws IOException {
        return format(prepare(correlation, response));
    }

    /**
     * Produces an HTTP-like response in individual lines.
     * <p>
     * Pr@param correlation the response correlation
     *
     * @param correlation the correlated request and response pair
     * @return a line-separated HTTP response
     * @throws IOException if reading body fails
     * @see #prepare(Precorrelation, HttpRequest)
     * @see #format(List)
     * @see StructuredHttpLogFormatter#prepare(Correlation, HttpResponse)
     */
    @API(status = EXPERIMENTAL)
    public List<String> prepare(final Correlation correlation, final HttpResponse response) throws IOException {
        final StringBuilder statusLineBuilder = new StringBuilder(64);
        statusLineBuilder.append(response.getProtocolVersion());
        statusLineBuilder.append(' ');
        statusLineBuilder.append(response.getStatus());
        final String reasonPhrase = response.getReasonPhrase();
        if(reasonPhrase != null) {
            statusLineBuilder.append(' ');
            statusLineBuilder.append(reasonPhrase);
        }
        return prepare(response, "Response", correlation.getId(),
                "Duration: " + correlation.getDuration().toMillis() + " ms", statusLineBuilder.toString());
    }

    private <H extends HttpMessage> List<String> prepare(final H message, final String type,
            final String correlationId, final String... prefixes) throws IOException {
        final List<String> lines = new ArrayList<>();

        lines.add(direction(message) + " " + type + ": " + correlationId);
        Collections.addAll(lines, prefixes);
        lines.addAll(formatHeaders(message));

        final String body = message.getBodyAsString();

        if (!body.isEmpty()) {
            lines.add("");
            lines.add(body);
        }

        return lines;
    }

    private String direction(final HttpMessage request) {
        return request.getOrigin() == REMOTE ? "Incoming" : "Outgoing";
    }

    private List<String> formatHeaders(final HttpMessage message) {
        return message.getHeaders().entrySet().stream()
                .collect(toMap(Map.Entry::getKey, this::formatHeaderValues))
                .entrySet().stream()
                .map(this::formatHeader)
                .collect(toList());
    }

    private String formatHeaderValues(final Map.Entry<String, List<String>> entry) {
        return String.join(", ", entry.getValue());
    }

    private String formatHeader(final Map.Entry<String, String> entry) {
        return String.format("%s: %s", entry.getKey(), entry.getValue());
    }

    /**
     * Renders an HTTP-like message into a printable string.
     *
     * @param lines lines of an HTTP message
     * @return the whole message as a single string, separated by new lines
     * @see #prepare(Precorrelation, HttpRequest)
     * @see #prepare(Correlation, HttpResponse)
     * @see StructuredHttpLogFormatter#format(Map)
     */
    @API(status = EXPERIMENTAL)
    public String format(final List<String> lines) {
        return String.join("\n", lines);
    }

}
