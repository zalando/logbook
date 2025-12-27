package org.zalando.logbook.json;

import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.ContentType;

import jakarta.annotation.Nullable;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * Fast alternative to {@link CompactingJsonBodyFilter} which just replaces all new lines, rather than
 * parsing and rewriting the JSON body.
 *
 * @see CompactingJsonBodyFilter
 */
@API(status = EXPERIMENTAL)
@Slf4j
public final class FastCompactingJsonBodyFilter implements BodyFilter {

    private final StringReplaceJsonCompactor compactor = new StringReplaceJsonCompactor();

    @Override
    public String filter(@Nullable final String contentType, final String body) {
        if (!ContentType.isJsonMediaType(contentType)) {
            return body;
        }

        return compactor.compact(body);
    }

}
