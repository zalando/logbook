package org.zalando.logbook.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;
import org.zalando.logbook.BodyFilter;

import javax.annotation.Nullable;
import java.io.IOException;

import static org.apiguardian.api.API.Status.MAINTAINED;

@API(status = MAINTAINED)
@Slf4j
public final class CompactingJsonBodyFilter implements BodyFilter.Default {

    private final JsonHeuristic heuristic = new JsonHeuristic();
    private final JsonCompactor compactor;

    public CompactingJsonBodyFilter() {
        this(new ObjectMapper());
    }

    public CompactingJsonBodyFilter(final ObjectMapper mapper) {
        this.compactor = new JsonCompactor(mapper);
    }

    @Override
    public String filter(@Nullable final String contentType, final String body) {
        return heuristic.isProbablyJson(contentType, body) && isCompactable(body) ? compact(body) : body;
    }

    private boolean isCompactable(final String body) {
        return !compactor.isCompacted(body);
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
