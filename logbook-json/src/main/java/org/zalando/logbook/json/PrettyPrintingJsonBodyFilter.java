package org.zalando.logbook.json;

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

    private final JsonHeuristic heuristic = new JsonHeuristic();

    private final ObjectMapper mapper;
    private final JsonCompactor compactor;
    private final ObjectWriter writer;

    public PrettyPrintingJsonBodyFilter() {
        this(new ObjectMapper());
    }

    public PrettyPrintingJsonBodyFilter(final ObjectMapper mapper) {
        this.mapper = mapper;
        this.compactor = new JsonCompactor(mapper);
        this.writer = mapper.writerWithDefaultPrettyPrinter();
    }

    @Override
    public String filter(@Nullable final String contentType, final String body) {
        return heuristic.isProbablyJson(contentType, body) && isProbablyNotPrettyPrinted(body) ? prettyPrint(body) : body;
    }

    private boolean isProbablyNotPrettyPrinted(final String body) {
        return compactor.isCompacted(body);
    }

    private String prettyPrint(final String body) {
        try {
            return writer.writeValueAsString(mapper.readTree(body));
        } catch (final IOException e) {
            log.trace("Unable to pretty print body, is it a JSON?. Keep it as-is: `{}`", e.getMessage());
            return body;
        }
    }

}
