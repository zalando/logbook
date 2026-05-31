package org.zalando.logbook.json;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;
import org.zalando.logbook.ContentType;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpMessage;
import org.zalando.logbook.StructuredHttpLogFormatter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.apiguardian.api.API.Status.STABLE;

/**
 * A custom {@link HttpLogFormatter} that produces JSON objects. It can be augmented with composition:
 *
 * <pre>
 * {@code
 *
 * public class CustomsFormatter implements HttpLogFormatter {
 *
 *     private final JsonHttpLogFormatter delegate;
 *
 *     public CustomsFormatter(ObjectMapper mapper) {
 *         this.delegate = new JsonHttpLogFormatter(mapper);
 *     }
 *
 *     public String format(Precorrelation precorrelation, HttpRequest request) throws IOException {
 *         Map<String, Object> content = delegate.prepare(precorrelation, request);
 *         // modify request here
 *         return delegate.format(content);
 *     }
 *
 *     public String format(Correlation correlation, HttpResponse response) throws IOException {
 *         Map<String, Object> content = delegate.prepare(correlation, response);
 *         // modify response here
 *         return delegate.format(content);
 *      }
 *
 * }
 * }
 * </pre>
 */
@API(status = STABLE)
@Deprecated(since = "4.0.0", forRemoval = true)
@Slf4j
public final class JsonHttpLogFormatterJackson2 implements StructuredHttpLogFormatter {

    private final ObjectMapper mapper;

    public JsonHttpLogFormatterJackson2() {
        this(new ObjectMapper());
    }

    public JsonHttpLogFormatterJackson2(final ObjectMapper mapper) {
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
        try (com.fasterxml.jackson.core.JsonParser parser = mapper.createParser(body)) {
            while (parser.nextToken() != null) {
                // consume all tokens to validate JSON
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
    private static final class JsonBody {
        String json;

        @JsonRawValue
        @JsonValue
        public String getJson() {
            return json;
        }
    }

}
