package org.zalando.logbook.json;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.zalando.logbook.Origin.LOCAL;

import java.io.IOException;

import org.apiguardian.api.API;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpMessage;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;

import com.fasterxml.jackson.core.JsonGenerator;

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
        
        write(request, generator);
	}

	@API(status = EXPERIMENTAL)
	default void write(Correlation correlation, HttpResponse response, JsonGenerator generator) throws IOException {
        generator.writeStringField("origin", getOrigin(response));
        generator.writeStringField("type", "response");
        generator.writeStringField("correlation", correlation.getId());
        generator.writeStringField("protocol", response.getProtocolVersion());
        generator.writeNumberField("duration", correlation.getDuration().toMillis());
        generator.writeNumberField("status", response.getStatus());
        
        write(response, generator);
	}

	static String getOrigin(HttpMessage message) {
		return message.getOrigin() == LOCAL ? "local" : "remote";
	}

}
