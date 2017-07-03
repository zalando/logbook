package org.zalando.logbook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * Formats requests as cURL commands.
 */
public final class CurlHttpLogFormatter implements HttpLogFormatter {

    private final HttpLogFormatter fallback;

    public CurlHttpLogFormatter() {
        this(new DefaultHttpLogFormatter());
    }

    public CurlHttpLogFormatter(final HttpLogFormatter fallback) {
        this.fallback = fallback;
    }

    @Override
    public String format(final Precorrelation<HttpRequest> precorrelation) throws IOException {
        final HttpRequest request = precorrelation.getRequest();
        final List<String> command = new ArrayList<>();

        command.add(precorrelation.getId());
        command.add("curl");
        command.add("-v"); // TODO optional?

        command.add("-X");
        command.add(request.getMethod());

        command.add(quote(request.getRequestUri()));

        request.getHeaders().forEach((header, values) -> {
            values.forEach(value -> {
                command.add("-H");
                command.add(quote(header + ": " + value));
            });
        });

        final String body = request.getBodyAsString();

        if (!body.isEmpty()) {
            command.add("--data-binary");
            command.add(quote(body));
        }

        return command.stream().collect(joining(" "));
    }

    private static String quote(final String s) {
        return "'" + escape(s) + "'";
    }

    private static String escape(final String s) {
        return s.replace("'", "\\'");
    }

    @Override
    public String format(final Correlation<HttpRequest, HttpResponse> correlation) throws IOException {
        return fallback.format(correlation);
    }

}
