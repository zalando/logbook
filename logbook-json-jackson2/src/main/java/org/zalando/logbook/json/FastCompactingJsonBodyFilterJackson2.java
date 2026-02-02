package org.zalando.logbook.json;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.ContentType;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * Fast alternative to {@link CompactingJsonBodyFilterJackson2} which just replaces all new lines, rather than
 * parsing and rewriting the JSON body.
 *
 * @see CompactingJsonBodyFilterJackson2
 */
@Deprecated(since = "4.0.0", forRemoval = true)
@API(status = EXPERIMENTAL)
@Slf4j
public final class FastCompactingJsonBodyFilterJackson2 implements BodyFilter {
    private final StringReplaceJsonCompactorJackson2 compactor = new StringReplaceJsonCompactorJackson2();
    @Override
    public String filter(@Nullable final String contentType, final String body) {
        if (!ContentType.isJsonMediaType(contentType)) {
            return body;
        }
        return compactor.compact(body);
    }
}
