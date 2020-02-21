package org.zalando.logbook;

import org.apiguardian.api.API;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public final class DefaultHttpLogFormatter implements HttpLogFormatter {

    /**
     * Produces an HTTP-like request in individual lines.
     *
     * @param precorrelation the request correlation
     * @param request        the HTTP request
     * @return a line-separated HTTP request
     * @throws IOException if reading body fails
     */
    @Override
    public String format(final Precorrelation precorrelation, final HttpRequest request) throws IOException {
        final String correlationId = precorrelation.getId();
        final String body = request.getBodyAsString();

        final StringBuilder result = new StringBuilder(body.length() + 2048);

        result.append(direction(request));
        result.append(" Request: ");
        result.append(correlationId);
        result.append('\n');

        result.append("Remote: ");
        result.append(request.getRemote());
        result.append('\n');

        result.append(request.getMethod());
        result.append(' ');
        RequestURI.reconstruct(request, result);
        result.append(' ');
        result.append(request.getProtocolVersion());
        result.append('\n');

        writeHeaders(request.getHeaders(), result);
        writeBody(body, result);

        return result.toString();
    }

    /**
     * Produces an HTTP-like request in individual lines.
     *
     * @param correlation the request correlation
     * @return a line-separated HTTP request
     * @throws IOException if reading body fails
     * @see StructuredHttpLogFormatter#prepare(Precorrelation, HttpRequest)
     */
    @Override
    public String format(final Correlation correlation, final HttpResponse response) throws IOException {
        final String correlationId = correlation.getId();
        final String body = response.getBodyAsString();

        final StringBuilder result = new StringBuilder(body.length() + 2048);

        result.append(direction(response));
        result.append(" Response: ");
        result.append(correlationId);
        result.append("\nDuration: ");
        result.append(correlation.getDuration().toMillis());
        result.append(" ms\n");

        result.append(response.getProtocolVersion());
        result.append(' ');
        result.append(response.getStatus());
        final String reasonPhrase = response.getReasonPhrase();
        if (reasonPhrase != null) {
            result.append(' ');
            result.append(reasonPhrase);
        }

        result.append('\n');

        writeHeaders(response.getHeaders(), result);
        writeBody(body, result);

        return result.toString();
    }

    private String direction(final HttpMessage request) {
        return request.getOrigin() == Origin.REMOTE ? "Incoming" : "Outgoing";
    }

    private void writeHeaders(final Map<String, List<String>> headers, final StringBuilder output) {
        if (headers.isEmpty()) {
            return;
        }

        for (final Entry<String, List<String>> entry : headers.entrySet()) {
            output.append(entry.getKey());
            output.append(": ");
            final List<String> headerValues = entry.getValue();
            if (!headerValues.isEmpty()) {
                for (final String value : entry.getValue()) {
                    output.append(value);
                    output.append(", ");
                }
                output.setLength(output.length() - 2); // discard last comma
            }
            output.append('\n');
        }
    }

    private void writeBody(final String body, final StringBuilder output) {
        if (!body.isEmpty()) {
            output.append('\n');
            output.append(body);
        } else {
            output.setLength(output.length() - 1); // discard last newline
        }
    }

}
