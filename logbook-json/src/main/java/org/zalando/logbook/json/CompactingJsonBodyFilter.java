package org.zalando.logbook.json;

import static org.apiguardian.api.API.Status.MAINTAINED;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.zalando.logbook.BodyFilter;

import lombok.extern.slf4j.Slf4j;

/**
 * @see FastCompactingJsonBodyFilter
 */
@API(status = MAINTAINED)
@Slf4j
public final class CompactingJsonBodyFilter implements BodyFilter {

    private final JsonCompactor compactor;

    public CompactingJsonBodyFilter(JsonCompactor compactor) {
        this.compactor = compactor;
    }
    
    public CompactingJsonBodyFilter() {
        this(new ParsingJsonCompactor());
    }

    @Override
    public String filter(@Nullable final String contentType, final String body) {
        if (!JsonMediaType.JSON.test(contentType)) {
            return body;
        }

        try {
            return compactor.compact(body);
        } catch (final IOException e) {
            log.trace("Unable to compact body, is it a JSON?. Keep it as-is: `{}`", e.getMessage());
            return body;
        }
    }

}
