package org.zalando.logbook.json;

import static org.apiguardian.api.API.Status.STABLE;

import java.io.IOException;
import java.io.StringWriter;
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
        this(objectMapper.getFactory());
    }

    @Override
    public String format(Precorrelation precorrelation, HttpRequest request) throws IOException {
        final String correlationId = precorrelation.getId();
        
        String body = request.getBodyAsString();

        StringWriter writer = new StringWriter(body.length() + 2048);
        
        try (JsonGenerator generator = jsonFactory.createGenerator(writer)) { 
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
            reconstructUri(request, generator);
            
            writeHeaders(generator, request);
    
            writeBody(request, body, generator);
    
            generator.writeEndObject();
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

        StringWriter writer = new StringWriter(body.length() + 2048);

        try (JsonGenerator generator = jsonFactory.createGenerator(writer)) {
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
    
    private void reconstructUri(final HttpRequest request, JsonGenerator generator) throws IOException {

        StringBuilder builder = new StringBuilder(256);

        final String scheme = request.getScheme();
        builder.append(scheme);
        builder.append("://");
        builder.append(request.getHost()); 
        final Optional<Integer> port = request.getPort();
        if (port.isPresent() && isNotStandardPort(scheme, port.get())) {
            builder.append(':').append(port.get());
        }
        builder.append(request.getPath());

        final String query = request.getQuery();
        if (!query.isEmpty()) {
            builder.append('?');

            builder.append(query);
        }
        generator.writeString(builder.toString());
    }

    private static boolean isNotStandardPort(final String scheme, final int port) {
        return ("http".equals(scheme) && port != 80) ||
                ("https".equals(scheme) && port != 443);
    }
    
}
