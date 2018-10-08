package org.zalando.logbook;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.function.Predicate;


@Slf4j
class JsonCompactingBodyFilter implements BodyFilter {

    private final JsonCompactor jsonCompactor;
    private final JsonHeuristic heuristic = new JsonHeuristic();
    private final Predicate<String> contentTypes = MediaTypeQuery.compile("application/json", "application/*+json");

    JsonCompactingBodyFilter(final ObjectMapper objectMapper) {
        jsonCompactor = new JsonCompactor(objectMapper);
    }

    @Override
    public String filter(@Nullable final String contentType, final String body) {
        return contentTypes.test(contentType) && shouldCompact(body) ? compact(body) : body;
    }

    private boolean shouldCompact(final String body) {
        return heuristic.isProbablyJson(body) && !jsonCompactor.isCompacted(body);
    }

    private String compact(final String body) {
        try {
            return jsonCompactor.compact(body);
        } catch (final IOException e) {
            log.trace("Unable to compact body, is it a JSON?. Keep it as-is: `{}`", e.getMessage());
            return body;
        }
    }
}
