package org.zalando.logbook.test;

import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.Sink;
import org.zalando.logbook.StructuredHttpLogFormatter;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * A sink that pretty-prints HTTP requests and responses as JSON to System.out,
 * using ANSI color codes for easier local development and debugging.
 */
public final class AnsiPrettyPrintingSink implements Sink {

    private final boolean useColor;
    private final StructuredHttpLogFormatter formatter;
    private final ObjectWriter prettyPrinter;

    /**
     * Default constructor auto-detects if a console is available to enable/disable colors.
     */
    public AnsiPrettyPrintingSink() {
        this(System.console() != null);
    }

    public AnsiPrettyPrintingSink(boolean useColor) {
        this.useColor = useColor;
        ObjectMapper mapper = new ObjectMapper();
        this.prettyPrinter = mapper.writerWithDefaultPrettyPrinter();
        
        // We implement StructuredHttpLogFormatter manually to avoid depending on logbook-json
        // and causing a cyclic dependency.
        this.formatter = new StructuredHttpLogFormatter() {
            @Override
            public String format(Map<String, Object> content) throws IOException {
                return prettyPrinter.writeValueAsString(content);
            }

            @Override
            public Optional<Object> prepareBody(final org.zalando.logbook.HttpMessage message) throws IOException {
                String body = message.getBodyAsString();
                if (body.isEmpty()) return Optional.empty();

                String contentType = message.getContentType();
                if (contentType != null && (contentType.startsWith("application/json") || contentType.contains("+json"))) {
                    try {
                        return Optional.of(mapper.readTree(body));
                    } catch (Exception e) {
                        // Fallback to raw string if invalid JSON
                    }
                }
                return Optional.of(body);
            }
        };
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void write(final Precorrelation precorrelation, final HttpRequest request) throws IOException {
        // Prepare the standard logbook map of request properties
        Map<String, Object> content = formatter.prepare(precorrelation, request);
        
        System.out.println(colorizeAndFormat(content));
    }

    @Override
    public void write(final Correlation correlation, final HttpRequest request, final HttpResponse response) throws IOException {
        // Prepare the standard logbook map of response properties
        Map<String, Object> content = formatter.prepare(correlation, response);
        
        System.out.println(colorizeAndFormat(content));
    }

    private String colorizeAndFormat(Map<String, Object> content) throws IOException {
        // Convert the map to a pretty-printed JSON string
        String json = prettyPrinter.writeValueAsString(content);

        if (!useColor) {
            return json;
        }

        // Apply basic ANSI coloring to the JSON string.
        // We use simple regex replacement for beginner-readable code.
        
        // 1. Color JSON keys in green (matches "key":)
        String colored = json.replaceAll("\"((?:\\\\\"|[^\"])+)\"(\\s*:)", AnsiColors.green("\"$1\"") + "$2");
        
        // 2. Color JSON string values in yellow (matches : "value")
        colored = colored.replaceAll("(:\\s*)\"((?:\\\\\"|[^\"])*)\"", "$1" + AnsiColors.yellow("\"$2\""));
        
        // 3. Highlight the "headers" key in cyan as requested
        colored = colored.replace(AnsiColors.green("\"headers\""), AnsiColors.cyan("\"headers\""));

        return colored;
    }
}
