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

    public JsonHttpLogFormatter() {
        this(new JsonMapper());
    }

    public JsonHttpLogFormatter(final JsonMapper mapper) {
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
            return Optional.of(tryParseAsJson(body));
        } else {
            return Optional.of(body);
        }
    }

    /**
     * Attempts to treat the body as a raw JSON value for embedding.
     * Uses a fast first-character check before attempting full JSON parsing.
     * If the body is not valid JSON, falls back to a plain quoted string
     * to ensure the overall log output remains valid JSON.
     */
    private Object tryParseAsJson(final String body) {
        // Fast path: obvious non-JSON content like ciphertext or binary data
        if (!looksLikeJson(body)) {
            return body;
        }

        // Slow path: streaming token validation — cheaper than readTree()
        // as it avoids building a full tree object in memory
        try (tools.jackson.core.JsonParser parser = mapper.createParser(body)) {
            while (parser.nextToken() != null) {
                // consume all tokens — if any are invalid, exception is thrown
            }
            return new JsonBody(body);
        } catch (final Exception e) {
            log.trace(
                    "Body has JSON content type but is not valid JSON, logging as string: `{}`",
                    e.getMessage()
            );
            return body;
        }
    }

    static boolean looksLikeJson(final String body) {
        if (body == null || body.isEmpty()) {
            return false;
        }
        final String trimmed = body.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        final char first = trimmed.charAt(0);
        final char last = trimmed.charAt(trimmed.length() - 1);

        // Valid JSON starts with: { [ " digit - t(rue) f(alse) n(ull)
        final boolean validStart = first == '{' || first == '['
                || first == '"'
                || (first >= '0' && first <= '9') || first == '-'
                || first == 't' || first == 'f' || first == 'n';

        // Valid JSON ends with: } ] " digit e(true/false) l(null) n
        final boolean validEnd = last == '}' || last == ']'
                || last == '"'
                || (last >= '0' && last <= '9')
                || last == 'e' || last == 'l' || last == 'n';

        return validStart && validEnd;
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
