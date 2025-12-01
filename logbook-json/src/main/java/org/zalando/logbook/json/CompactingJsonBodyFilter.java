package org.zalando.logbook.json;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.ContentType;

import javax.annotation.Nullable;
import java.io.IOException;

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

    public CompactingJsonBodyFilter(final JsonGeneratorWrapperJackson3 jsonGeneratorWrapper) {
        this(new ParsingJsonCompactorJackson3(jsonGeneratorWrapper));
    }

    @Generated
    public CompactingJsonBodyFilter() {
        this(createDefaultCompactor());
    }

    @lombok.Generated
    private static JsonCompactor createDefaultCompactor() {
        try {
            // Try Jackson 3 first when explicitly requested
            Class.forName("tools.jackson.core.json.JsonFactory");
            return new ParsingJsonCompactorJackson3();
        } catch (final ClassNotFoundException e) {
            return new ParsingJsonCompactor();
        }
    }

    @Override
    public String filter(@Nullable final String contentType, final String body) {
        if (!ContentType.isJsonMediaType(contentType)) {
            return body;
        }

        try {
            return compactor.compact(body);
        } catch (final IOException e) {
            log.trace("Unable to compact body, is it a JSON?. Keep it as-is: `{}`", e.getMessage());
            return body;
        } catch (final RuntimeException e) {
            // Handle Jackson parsing errors (both Jackson 2 and 3)
            // Note: This catch is for Jackson 3 which throws RuntimeExceptions instead of IOExceptions
            log.trace("Unable to compact body, is it a JSON?. Keep it as-is: `{}`", e.getMessage());
            return body;
        }
    }

}
