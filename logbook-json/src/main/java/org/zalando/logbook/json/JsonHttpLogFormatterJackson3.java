package org.zalando.logbook.json;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Generated;
import org.apiguardian.api.API;
import org.zalando.logbook.ContentType;
import org.zalando.logbook.HttpMessage;
import org.zalando.logbook.StructuredHttpLogFormatter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.apiguardian.api.API.Status.STABLE;

/**
 * JSON formatter for Jackson 3.x (tools.jackson namespace).
 */
@API(status = STABLE)
@Generated
public final class JsonHttpLogFormatterJackson3 implements StructuredHttpLogFormatter {

    private final ObjectMapper mapper;

    public JsonHttpLogFormatterJackson3() {
        this(new ObjectMapper());
    }

    public JsonHttpLogFormatterJackson3(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<Object> prepareBody(final HttpMessage message) throws IOException {
        final String contentType = message.getContentType();
        final String body = message.getBodyAsString();
        if (body.isEmpty()) {
            return Optional.empty();
        }
        if (ContentType.isJsonMediaType(contentType)) {
            return Optional.of(new JsonBody(body));
        } else {
            return Optional.of(body);
        }
    }

    @Override
    public String format(final Map<String, Object> content) throws IOException {
        return mapper.writeValueAsString(content);
    }

    @AllArgsConstructor
    @Generated
    private static final class JsonBody {
        String json;

        @JsonRawValue
        @JsonValue
        public String getJson() {
            return json;
        }
    }
}
