package org.zalando.logbook.json;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;
import org.zalando.logbook.ContentType;
import org.zalando.logbook.HttpMessage;
import org.zalando.logbook.StructuredHttpLogFormatter;
import tools.jackson.databind.json.JsonMapper;

import static org.apiguardian.api.API.Status.STABLE;

/**
 * JSON formatter for Jackson 3.x (tools.jackson namespace).
 */
@API(status = STABLE)
@Generated
@Slf4j
public final class JsonHttpLogFormatter implements StructuredHttpLogFormatter {

    private final JsonMapper mapper;
    private final boolean validateJsonBody;

    public JsonHttpLogFormatter() {
        this(new JsonMapper());
    }

    public JsonHttpLogFormatter(final JsonMapper mapper) {
        this(mapper, false);
    }

    public JsonHttpLogFormatter(final JsonMapper mapper, final boolean validateJsonBody) {
        this.mapper = mapper;
        this.validateJsonBody = validateJsonBody;
    }

    @Override
    public Optional<Object> prepareBody(final HttpMessage message) throws IOException {
        final String contentType = message.getContentType();
        final String body = message.getBodyAsString();

        if (body.isEmpty()) {
            return Optional.empty();
        }

        if (ContentType.isJsonMediaType(contentType)) {
            if (JsonUtil.looksLikeJson(body) && (!validateJsonBody || JsonUtil.isValidJson(body, mapper))) {
                return Optional.of(new JsonBody(body));
            } else {
                return Optional.of(body);
            }
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
