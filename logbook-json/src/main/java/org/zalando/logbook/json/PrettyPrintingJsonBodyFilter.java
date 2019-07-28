package org.zalando.logbook.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;
import org.zalando.logbook.BodyFilter;

import javax.annotation.Nullable;
import java.io.IOException;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
@Slf4j
public final class PrettyPrintingJsonBodyFilter implements BodyFilter {

    private final ObjectMapper mapper;
    private final ObjectWriter writer;

    public PrettyPrintingJsonBodyFilter() {
        this(new ObjectMapper());
    }

    public PrettyPrintingJsonBodyFilter(final ObjectMapper mapper) {
        this.mapper = mapper;
        this.writer = mapper.writerWithDefaultPrettyPrinter();
    }

    @Override
    public String filter(@Nullable final String contentType, final String body) {
        if (!JsonMediaType.JSON.test(contentType)) {
            return body;
        }

        if (body.indexOf('\n') != -1) {
            // probably already pretty printed
            return body;
        }

        if (body.isEmpty()) {
            return body;
        }

        try {
            final JsonNode value = mapper.readTree(body);
            return writer.writeValueAsString(value);
        } catch (final IOException e) {
            log.trace("Unable to pretty print body. Is it JSON?. Keep it as-is: `{}`", e.getMessage());
            return body;
        }
    }

}
