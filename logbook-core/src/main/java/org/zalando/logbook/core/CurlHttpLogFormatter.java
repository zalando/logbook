package org.zalando.logbook.core;

import org.apiguardian.api.API;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * Formats requests as cURL commands.
 */
@API(status = EXPERIMENTAL)
public final class CurlHttpLogFormatter implements HttpLogFormatter {

    private final HttpLogFormatter fallback;

    public CurlHttpLogFormatter() {
        this(new DefaultHttpLogFormatter());
    }

    public CurlHttpLogFormatter(final HttpLogFormatter fallback) {
        this.fallback = fallback;
    }

    @Override
    public String format(final Precorrelation precorrelation, final HttpRequest request) throws IOException {
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

        return String.join(" ", command);
    }

    private static String quote(final String s) {
        return "'" + escape(s) + "'";
    }

    private static String escape(final String s) {
        return s.replace("'", "\\'");
    }

    @Override
    public String format(final Correlation correlation, final HttpResponse response)
            throws IOException {
        return fallback.format(correlation, response);
    }

}
