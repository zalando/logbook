package org.zalando.logbook.json;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apiguardian.api.API;
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
 *     public CustomsFormatter(final ObjectMapper mapper) {
 *         this.delegate = new JsonHttpLogFormatter(mapper);
 *     }
 *
 *     public String format(final Precorrelation<HttpRequest> precorrelation) throws IOException {
 *         Map<String, Object> request = delegate.prepare(precorrelation);
 *         // modify request here
 *         return delegate.format(request);
 *     }
 *
 *     public String format(final Correlation<HttpRequest, HttpResponse> correlation) throws IOException {
 *         Map<String, Object> response = delegate.prepare(correlation);
 *         // modify response here
 *         return delegate.format(response);
 *      }
 *
 * }
 * }
 * </pre>
 */
@API(status = STABLE)
public final class JsonHttpLogFormatter implements StructuredHttpLogFormatter {

    private final ObjectMapper mapper;
    private final JsonHeuristic heuristic = new JsonHeuristic();

    public JsonHttpLogFormatter() {
        this(new ObjectMapper());
    }

    public JsonHttpLogFormatter(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<Object> prepareBody(final HttpMessage message) throws IOException {
        final String contentType = message.getContentType();
        final String body = message.getBodyAsString();

        if (heuristic.isProbablyJson(contentType, body)) {
            return Optional.of(new JsonBody(body));
        } else {
            return Optional.ofNullable(body.isEmpty() ? null : body);
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
