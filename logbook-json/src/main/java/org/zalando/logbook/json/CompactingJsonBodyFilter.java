package org.zalando.logbook.json;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.ContentType;

import static org.apiguardian.api.API.Status.MAINTAINED;

/**
 * @see FastCompactingJsonBodyFilter
 */
@API(status = MAINTAINED)
@Slf4j
@AllArgsConstructor
public final class CompactingJsonBodyFilter implements BodyFilter {

    private final JsonCompactor compactor;

    public CompactingJsonBodyFilter(final JsonGeneratorWrapper jsonGeneratorWrapper) {
        this(new ParsingJsonCompactor(jsonGeneratorWrapper));
    }

    @Generated
    public CompactingJsonBodyFilter() {
        this(createDefaultCompactor());
    }

    @lombok.Generated
    private static JsonCompactor createDefaultCompactor() {
        try {
            // If we don't find Jackson 3 on the classpath then return a Noop compactor, as we still can initialize the
            // Jackson 2 Compactor provided by the legacy logbook-json-jackson2 module
            Class.forName("tools.jackson.core.json.JsonFactory");
            return new ParsingJsonCompactor();
        } catch (final ClassNotFoundException e) {
            return new NoopJsonCompactor();
        }
    }

    @Override
    public String filter(@Nullable final String contentType, final String body) {
        if (!ContentType.isJsonMediaType(contentType)) {
            return body;
        }

        try {
            return compactor.compact(body);
        } catch (final RuntimeException e) {
            log.trace("Unable to compact body, is it a JSON?. Keep it as-is: `{}`", e.getMessage());
            return body;
        }
    }

}
