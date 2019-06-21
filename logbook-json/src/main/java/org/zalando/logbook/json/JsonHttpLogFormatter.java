package org.zalando.logbook.json;

import static org.apiguardian.api.API.Status.STABLE;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apiguardian.api.API;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpMessage;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;
import org.zalando.logbook.Precorrelation;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.core.util.BufferRecyclers;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A custom {@link HttpLogFormatter} that produces JSON objects. 
 */
@API(status = STABLE)
public final class JsonHttpLogFormatter implements HttpLogFormatter {

    private final JsonFactory jsonFactory;
    
    public JsonHttpLogFormatter() {
        this(new ObjectMapper());
    }
    
    public JsonHttpLogFormatter(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    public JsonHttpLogFormatter(ObjectMapper objectMapper) {
        this.jsonFactory = objectMapper.getFactory();
    }

    @Override
    public String format(Precorrelation precorrelation, HttpRequest request) throws IOException {
        final String correlationId = precorrelation.getId();
        
        String body = request.getBodyAsString();

        StringBuilderWriter writer = new StringBuilderWriter(body.length() + 2048);

        JsonGenerator generator = jsonFactory.createGenerator(writer);
        try {
            generator.writeStartObject();
            
            generator.writeFieldName("origin");
            if(request.getOrigin() == Origin.LOCAL) {
                generator.writeRawValue("\"local\"");
            } else {
                generator.writeRawValue("\"remote\"");
            }
            generator.writeFieldName("type");
            generator.writeRawValue("\"request\"");
            generator.writeStringField("correlation", correlationId);
            generator.writeStringField("protocol", request.getProtocolVersion());
            generator.writeStringField("remote", request.getRemote());
            
            generator.writeStringField("method", request.getMethod());

            generator.writeFieldName("uri");
            reconstruct(request, generator, writer);
            
            writeHeaders(generator, request);
    
            writeBody(request, body, generator);
    
            generator.writeEndObject();
        } finally {
            generator.close();
        }
        
        return writer.toString();
            
    }

    private void writeBody(HttpMessage message, String body, JsonGenerator builder) throws IOException {
        if(!body.isEmpty()) {
            builder.writeFieldName("body");

            final String contentType = message.getContentType();

            if (JsonMediaType.JSON.test(contentType)) {
                builder.writeRawValue(body);
            } else {
                builder.writeString(body);
            }
        }
    }

    @Override
    public String format(Correlation correlation, HttpResponse response) throws IOException {
        
        final String correlationId = correlation.getId();
        
        String body = response.getBodyAsString();

        StringBuilderWriter writer = new StringBuilderWriter(body.length() + 2048);

        JsonGenerator generator = jsonFactory.createGenerator(writer);

        try {
            generator.writeStartObject();
    
            generator.writeFieldName("origin");
            if(response.getOrigin() == Origin.LOCAL) {
                generator.writeRawValue("\"local\"");
            } else {
                generator.writeRawValue("\"remote\"");
            }
            generator.writeFieldName("type");
            generator.writeRawValue("\"response\"");
            generator.writeStringField("correlation", correlationId);
            generator.writeStringField("protocol", response.getProtocolVersion());
            generator.writeNumberField("duration", correlation.getDuration().toMillis());
            generator.writeNumberField("status", response.getStatus());
    
            writeHeaders(generator, response);
            writeBody(response, body, generator);
            
            generator.writeEndObject();
        } finally {
            generator.close();
        }
        return writer.toString();
    }

    protected void writeHeaders(JsonGenerator builder, HttpMessage httpMessage) throws IOException {
        Map<String, List<String>> headers = httpMessage.getHeaders();
        
        if(!headers.isEmpty()) {
            // implementation note:
            // for some unclear reason, manually iterating over the headers
            // while writing performs worse than letting Jackson do the job.
            builder.writeObjectField("headers", headers);
        }
    }
    
    private void reconstruct(final HttpRequest request, JsonGenerator generator, StringBuilderWriter writer) throws IOException {

        generator.writeRawValue("\"");

        // write to underlying stream to avoid creating objects
        
        // first flush the json generator
        generator.flush();

        // then write to the underlying writer / builder
        StringBuilder builder = writer.getBuilder();

        final String scheme = request.getScheme();
        builder.append(scheme);
        builder.append("://"); // forward slash escaping is optional, so don't escape
        
        // host legal characters: Letters, Numbers 0-9 and Hyphen - none of which are escaped in JSON
        builder.append(request.getHost()); 

        final Optional<Integer> port = request.getPort();
        if(port.isPresent() && isNotStandardPort(scheme, port.get())) {
            builder.append(':').append(port.get());
        }
        
        JsonStringEncoder jsonStringEncoder = BufferRecyclers.getJsonStringEncoder();
        jsonStringEncoder.quoteAsString(request.getPath(), builder); // escape

        final String query = request.getQuery();
        if (!query.isEmpty()) {
            builder.append('?');

            jsonStringEncoder.quoteAsString(query, builder); // escape
        }
        
        builder.append('"');
    }

    private static boolean isNotStandardPort(final String scheme, final int port) {
        return ("http".equals(scheme) && port != 80) ||
                ("https".equals(scheme) && port != 443);
    }
    
}
