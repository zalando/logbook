package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apiguardian.api.API;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpMessage;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.STABLE;
import static org.zalando.logbook.Origin.LOCAL;

/**
 * A custom {@link HttpLogFormatter} that produces JSON objects.
 */
@API(status = STABLE)
@AllArgsConstructor
public final class FastJsonHttpLogFormatter implements HttpLogFormatter {

    private final JsonFactory factory;

    public FastJsonHttpLogFormatter() {
        this(new ObjectMapper());
    }

    public FastJsonHttpLogFormatter(final ObjectMapper mapper) {
        this(mapper.getFactory());
    }

    @Override
    public String format(
            final Precorrelation precorrelation,
            final HttpRequest request) throws IOException {

        return format(precorrelation, request, this::prepare);
    }

    @API(status = EXPERIMENTAL)
    public void prepare(
            final Precorrelation precorrelation,
            final HttpRequest request,
            final JsonGenerator generator) throws IOException {

        generator.writeStringField("origin",
                request.getOrigin() == LOCAL ? "local" : "remote");
        generator.writeStringField("type", "request");
        generator.writeStringField("correlation", precorrelation.getId());
        generator.writeStringField("protocol", request.getProtocolVersion());
        generator.writeStringField("remote", request.getRemote());
        generator.writeStringField("method", request.getMethod());
        generator.writeStringField("uri", reconstructUri(request));

        writeHeaders(request, generator);
        writeBody(request, generator);
    }

    @Override
    public String format(
            final Correlation correlation,
            final HttpResponse response) throws IOException {

        return format(correlation, response, this::prepare);
    }

    @API(status = EXPERIMENTAL)
    public void prepare(
            final Correlation correlation,
            final HttpResponse response,
            final JsonGenerator generator) throws IOException {

        final String correlationId = correlation.getId();

        generator.writeStringField("origin",
                response.getOrigin() == LOCAL ? "local" : "remote");
        generator.writeStringField("type", "response");
        generator.writeStringField("correlation", correlationId);
        generator.writeStringField("protocol", response.getProtocolVersion());
        generator.writeNumberField("duration", correlation.getDuration().toMillis());
        generator.writeNumberField("status", response.getStatus());

        writeHeaders(response, generator);
        writeBody(response, generator);
    }

    private <C extends Precorrelation, H extends HttpMessage> String format(
            final C correlation,
            final H message,
            final Formatter<C, H> formatter) throws IOException {

        final StringWriter writer = new StringWriter(message.getBody().length + 2048);

        try (final JsonGenerator generator = factory.createGenerator(writer)) {
            generator.writeStartObject();
            formatter.format(correlation, message, generator);
            generator.writeEndObject();
        }

        return writer.toString();
    }

    @FunctionalInterface
    private interface Formatter<C extends Precorrelation, H extends HttpMessage> {
        void format(C correlation, H message, JsonGenerator generator) throws IOException;
    }

    private void writeHeaders(
            final HttpMessage message,
            final JsonGenerator generator) throws IOException {

        final Map<String, List<String>> headers = message.getHeaders();

        if (headers.isEmpty()) {
            return;
        }

        // implementation note:
        // for some unclear reason, manually iterating over the headers
        // while writing performs worse than letting Jackson do the job.
        generator.writeObjectField("headers", headers);
    }

    private void writeBody(
            final HttpMessage message,
            final JsonGenerator generator) throws IOException {

        final String body = message.getBodyAsString();

        if (body.isEmpty()) {
            return;
        }
        generator.writeFieldName("body");

        final String contentType = message.getContentType();

        if (JsonMediaType.JSON.test(contentType)) {
            generator.writeRawValue(body);
        } else {
            generator.writeString(body);
        }
    }

    private String reconstructUri(final HttpRequest request) {
        final StringBuilder builder = new StringBuilder(256);

        final String scheme = request.getScheme();
        builder.append(scheme);
        builder.append("://");
        builder.append(request.getHost());
        appendPort(request, builder);
        builder.append(request.getPath());
        appendQuery(request, builder);

        return builder.toString();
    }

    private void appendPort(final HttpRequest request, final StringBuilder builder) {
        request.getPort().ifPresent(port -> {
            final String scheme = request.getScheme();
            if (isStandardPort(scheme, port)) {
                return;
            }

            builder.append(':').append(port);
        });
    }

    private void appendQuery(final HttpRequest request, final StringBuilder builder) {
        final String query = request.getQuery();

        if (query.isEmpty()) {
            return;
        }

        builder.append('?');
        builder.append(query);
    }

    private boolean isStandardPort(final String scheme, final int port) {
        return ("http".equals(scheme) && port == 80)
                || ("https".equals(scheme) && port == 443);
    }

}
