package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;
import org.zalando.logbook.BodyFilter;

import javax.annotation.Nullable;
import java.io.IOException;

import static org.apiguardian.api.API.Status.MAINTAINED;

/**
 * @see FastCompactingJsonBodyFilter
 */
@API(status = MAINTAINED)
@Slf4j
public final class CompactingJsonBodyFilter implements BodyFilter {

    private final JsonCompactor compactor;

    public CompactingJsonBodyFilter() {
        this(new ObjectMapper());
    }

    public CompactingJsonBodyFilter(final JsonFactory factory) {
        this.compactor = new ParsingJsonCompactor(factory);
    }

    public CompactingJsonBodyFilter(final ObjectMapper mapper) {
        this(mapper.getFactory());
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
