package org.zalando.logbook;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apiguardian.api.API;

import java.io.IOException;
import java.util.Map;
import java.util.function.Predicate;

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
public final class JsonHttpLogFormatter implements PreparedHttpLogFormatter {

    private static final Predicate<String> JSON = MediaTypeQuery.compile("application/json", "application/*+json");

    private final ObjectMapper mapper;
    private final JsonHeuristic heuristic = new JsonHeuristic();

    public JsonHttpLogFormatter() {
        this(new ObjectMapper());
    }

    public JsonHttpLogFormatter(final ObjectMapper mapper) {
        this.mapper = mapper;
    }


    @Override
    public String format(final Map<String, Object> content) throws IOException {
        return mapper.writeValueAsString(content);
    }

    @Override
    public void addBody(final HttpMessage message, final Map<String, Object> content) throws IOException {
        if (isContentTypeJson(message)) {
            content.put("body", treatPossibleJsonAsJson(message.getBodyAsString()));
        } else {
            PreparedHttpLogFormatter.super.addBody(message, content);
        }
    }

    private boolean isContentTypeJson(final HttpMessage message) {
        return JSON.test(message.getContentType());
    }

    private Object treatPossibleJsonAsJson(final String body) {
        if (heuristic.isProbablyJson(body)) {
            return new JsonBody(body);
        } else {
            return body;
        }
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
