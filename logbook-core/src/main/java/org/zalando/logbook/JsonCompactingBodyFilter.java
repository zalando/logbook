package org.zalando.logbook;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.function.Predicate;

@Slf4j
final class JsonCompactingBodyFilter implements BodyFilter {

    private static final Predicate<String> JSON = MediaTypeQuery.compile("application/json", "application/*+json");

    private final JsonHeuristic heuristic = new JsonHeuristic();
    private final JsonCompactor compactor;

    JsonCompactingBodyFilter(final ObjectMapper objectMapper) {
        this.compactor = new JsonCompactor(objectMapper);
    }

    @Override
    public String filter(@Nullable final String contentType, final String body) {
        return JSON.test(contentType) && isCompactable(body) ? compact(body) : body;
    }

    private boolean isCompactable(final String body) {
        return heuristic.isProbablyJson(body) && !compactor.isCompacted(body);
    }

    private String compact(final String body) {
        try {
            return compactor.compact(body);
        } catch (final IOException e) {
            log.trace("Unable to compact body, is it a JSON?. Keep it as-is: `{}`", e.getMessage());
            return body;
        }
    }

}
