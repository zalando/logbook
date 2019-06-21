package org.zalando.logbook.json;

import static org.apiguardian.api.API.Status.STABLE;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;

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
        this.jsonFactory = objectMapper.getFactory();
    }

    @Override
    public String format(Precorrelation precorrelation, HttpRequest request) throws IOException {
        final String correlationId = precorrelation.getId();
        
        String body = request.getBodyAsString();

        StringWriter writer = new StringWriter(body.length() + 2048);

        JsonGenerator generator = jsonFactory.createGenerator(writer);
        try {
            generator.writeStartObject();
            
            if(request.getOrigin() == Origin.LOCAL) {
                generator.writeStringField("origin", "local");
            } else {
                generator.writeStringField("origin", "remote");
            }
            generator.writeStringField("type", "request");
            generator.writeStringField("correlation", correlationId);
            generator.writeStringField("protocol", request.getProtocolVersion());
            generator.writeStringField("remote", request.getRemote());
            generator.writeStringField("method", request.getMethod());
            generator.writeFieldName("uri");
            reconstruct(request, generator);
            
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

        StringWriter writer = new StringWriter(body.length() + 2048);

        JsonGenerator generator = jsonFactory.createGenerator(writer);

        try {
            generator.writeStartObject();
    
            
            if(response.getOrigin() == Origin.LOCAL) {
                generator.writeStringField("origin", "local");
            } else {
                generator.writeStringField("origin", "remote");
            }
            generator.writeStringField("type", "response");
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
            builder.writeObjectFieldStart("headers");
            
            for (Entry<String, List<String>> entry : headers.entrySet()) {
                
                builder.writeArrayFieldStart(entry.getKey());
                for(String value : entry.getValue()) {
                    builder.writeString(value);
                }
                builder.writeEndArray();
            }
            builder.writeEndObject();
        }
    }
    
    private void reconstruct(final HttpRequest request, JsonGenerator generator) throws IOException {
        final String scheme = request.getScheme();
        final String host = request.getHost();
        final Optional<Integer> port = request.getPort();
        final String path = request.getPath();
        final String query = request.getQuery();

        StringBuilder url = new StringBuilder(1024);
        url.append(scheme);
        url.append("://");
        url.append(host);

        port.ifPresent(p -> {
            if (isNotStandardPort(scheme, p)) {
                url.append(':').append(p);
            }
        });

        url.append(path);

        if (!query.isEmpty()) {
            url.append('?');
            url.append(query);
        }
        
        generator.writeString(url.toString());
    }

    private static boolean isNotStandardPort(final String scheme, final int port) {
        return ("http".equals(scheme) && port != 80) ||
                ("https".equals(scheme) && port != 443);
    }
    
}

