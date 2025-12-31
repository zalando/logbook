package org.zalando.logbook.json;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
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
            // TODO has this JSON been validated? If not then this might result in invalid log statements
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
    private static final class JsonBody {
        String json;

        @JsonRawValue
        @JsonValue
        public String getJson() {
            return json;
        }
    }

}
