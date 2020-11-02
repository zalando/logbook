package org.zalando.logbook.json;

import static org.apiguardian.api.API.Status.STABLE;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.apiguardian.api.API;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpMessage;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * A custom {@link HttpLogFormatter} that produces JSON objects.
 */
@API(status = STABLE)
@RequiredArgsConstructor
public final class FastJsonHttpLogFormatter implements HttpLogFormatter, JsonFieldWriter {

    private final JsonFactory factory;

    private JsonFieldWriter delegate = this; // default to this implementation

    public FastJsonHttpLogFormatter() {
        this(new ObjectMapper());
    }

    public FastJsonHttpLogFormatter(final ObjectMapper mapper) {
        this(mapper.getFactory());
    }

    public FastJsonHttpLogFormatter(final ObjectMapper mapper,
    		final JsonFieldWriter writer) {
        this(mapper.getFactory());
        this.delegate = writer;
    }

    @FunctionalInterface
    private interface Formatter<C extends Precorrelation, H extends HttpMessage> {
        void format(C correlation, H message, JsonGenerator generator) throws IOException;
    }

    @Override
    public String format(
            final Precorrelation precorrelation,
            final HttpRequest request) throws IOException {

        return format(precorrelation, request, delegate::write);
    }

    @Override
    public String format(
            final Correlation correlation,
            final HttpResponse response) throws IOException {

        return format(correlation, response, delegate::write);
    }

    private <C extends Precorrelation, H extends HttpMessage> String format(
            final C correlation,
            final H message,
            final Formatter<C, H> formatter) throws IOException {

        final StringWriter writer = new StringWriter(message.getBody().length + 2048);

        try (final JsonGenerator generator = factory.createGenerator(writer)) {
            generator.writeStartObject();
            formatter.format(correlation, message, generator);
            delegate.write(message, generator);
            generator.writeEndObject();
        }

        return writer.toString();
    }

    @Override
    public <M extends HttpMessage> void write(M message, JsonGenerator generator) throws IOException {
    	writeHeaders(message, generator);
    	writeBody(message, generator);
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

}
