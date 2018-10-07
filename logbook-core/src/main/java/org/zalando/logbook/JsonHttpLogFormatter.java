package org.zalando.logbook;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 *         // TODO modify request here
 *         return delegate.format(request);
 *     }
 *
 *     public String format(final Correlation<HttpRequest, HttpResponse> correlation) throws IOException {
 *         Map<String, Object> response = delegate.prepare(correlation);
 *         // TODO modify response here
 *         return delegate.format(response);
 *      }
 *
 * }
 * }
 * </pre>
 */
@API(status = STABLE)
public final class JsonHttpLogFormatter extends AbstractPreparedHttpLogFormatter {

    private static final Logger LOG = LoggerFactory.getLogger(JsonHttpLogFormatter.class);
    private static final Predicate<String> JSON = MediaTypeQuery.compile("application/json", "application/*+json");

    private final ObjectMapper mapper;
    private final JsonCompactor compactor;
    private final JsonHeuristic heuristic = new JsonHeuristic();

    public JsonHttpLogFormatter() {
        this(new ObjectMapper());
    }

    public JsonHttpLogFormatter(final ObjectMapper mapper) {
        this.mapper = mapper;
        this.compactor = new JsonCompactor(mapper);
    }


    @Override
    public String format(final Map<String, Object> content) throws IOException {
        return mapper.writeValueAsString(content);
    }

    @Override
    protected void addBody(final HttpMessage message, final Map<String, Object> content) throws IOException {
        if (isContentTypeJson(message)) {
            content.put("body", tryParseBodyAsJson(message.getBodyAsString()));
        } else {
            super.addBody(message, content);
        }
    }

    private boolean isContentTypeJson(final HttpMessage message) {
        return JSON.test(message.getContentType());
    }

    private Object tryParseBodyAsJson(final String body) {
        if (heuristic.isProbablyJson(body)) {
            if (compactor.isCompacted(body)) {
                // any body that looks like JSON (according to our heuristic) and has no newlines would be
                // incorrectly treated as JSON here which results in an invalid JSON output
                // see https://github.com/zalando/logbook/issues/279
                return new JsonBody(body);
            }

            try {
                return new JsonBody(compactor.compact(body));
            } catch (final IOException e) {
                LOG.trace("Unable to compact body, probably because it's not JSON. Embedding it as-is: [{}]", e.getMessage());
                return body;
            }
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
