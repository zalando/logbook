package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonGenerator;
import org.apiguardian.api.API;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpMessage;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.zalando.logbook.Origin.LOCAL;

public interface JsonFieldWriter {

    <M extends HttpMessage> void write(M message, JsonGenerator generator) throws IOException;

    @API(status = EXPERIMENTAL)
    default void write(Precorrelation correlation, HttpRequest request, JsonGenerator generator) throws IOException {
        generator.writeStringField("origin", getOrigin(request));
        generator.writeStringField("type", "request");
        generator.writeStringField("correlation", correlation.getId());
        generator.writeStringField("protocol", request.getProtocolVersion());
        generator.writeStringField("remote", request.getRemote());
        generator.writeStringField("method", request.getMethod());
        generator.writeStringField("uri", request.getRequestUri());
    }

    @API(status = EXPERIMENTAL)
    default void write(Correlation correlation, HttpResponse response, JsonGenerator generator) throws IOException {
        generator.writeStringField("origin", getOrigin(response));
        generator.writeStringField("type", "response");
        generator.writeStringField("correlation", correlation.getId());
        generator.writeStringField("protocol", response.getProtocolVersion());
        generator.writeNumberField("duration", correlation.getDuration().toMillis());
        generator.writeNumberField("status", response.getStatus());
    }

    static String getOrigin(HttpMessage message) {
        return message.getOrigin() == LOCAL ? "local" : "remote";
    }

}
