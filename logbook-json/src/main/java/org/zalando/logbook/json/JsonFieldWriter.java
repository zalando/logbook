package org.zalando.logbook.json;

import org.apiguardian.api.API;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpMessage;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;
import tools.jackson.core.JsonGenerator;

import java.io.IOException;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.zalando.logbook.Origin.LOCAL;

public interface JsonFieldWriter {

    <M extends HttpMessage> void write(M message, JsonGenerator generator) throws IOException;

    @API(status = EXPERIMENTAL)
    default void write(Precorrelation correlation, HttpRequest request, JsonGenerator generator) throws IOException {
        generator.writeName("origin");
        generator.writeString(getOrigin(request));
        generator.writeName("type");
        generator.writeString("request");
        generator.writeName("correlation");
        generator.writeString(correlation.getId());
        generator.writeName("protocol");
        generator.writeString(request.getProtocolVersion());
        generator.writeName("remote");
        generator.writeString(request.getRemote());
        generator.writeName("method");
        generator.writeString(request.getMethod());
        generator.writeName("uri");
        generator.writeString(request.getRequestUri());
    }

    @API(status = EXPERIMENTAL)
    default void write(Correlation correlation, HttpResponse response, JsonGenerator generator) throws IOException {
        generator.writeName("origin");
        generator.writeString(getOrigin(response));
        generator.writeName("type");
        generator.writeString("response");
        generator.writeName("correlation");
        generator.writeString(correlation.getId());
        generator.writeName("protocol");
        generator.writeString(response.getProtocolVersion());
        generator.writeName("duration");
        generator.writeNumber(correlation.getDuration().toMillis());
        generator.writeName("status");
        generator.writeNumber(response.getStatus());
    }

    static String getOrigin(HttpMessage message) {
        return message.getOrigin() == LOCAL ? "local" : "remote";
    }

}
