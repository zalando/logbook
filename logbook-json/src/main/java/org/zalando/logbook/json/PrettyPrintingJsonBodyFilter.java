package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.ContentType;

import javax.annotation.Nullable;
import java.io.CharArrayWriter;
import java.io.IOException;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
@Slf4j
public final class PrettyPrintingJsonBodyFilter implements BodyFilter {

    private final JsonFactory factory;
    private final boolean usePreciseFloats;

    public PrettyPrintingJsonBodyFilter(final JsonFactory factory,
                                        final boolean usePreciseFloats) {
        this.factory = factory;
        this.usePreciseFloats = usePreciseFloats;
    }

    public PrettyPrintingJsonBodyFilter(final JsonFactory factory) {
        this(factory, false);
    }

    public PrettyPrintingJsonBodyFilter() {
        this(new JsonFactory());
    }

    public PrettyPrintingJsonBodyFilter(final ObjectMapper mapper) {
        this(mapper.getFactory());
    }

    @Override
    public String filter(@Nullable final String contentType, final String body) {
        if (!ContentType.isJsonMediaType(contentType)) {
            return body;
        }

        if (body.indexOf('\n') != -1) {
            // probably already pretty printed
            return body;
        }

        try (
                final CharArrayWriter output = new CharArrayWriter(body.length() * 2); // rough estimate of output size
                final JsonParser parser = factory.createParser(body);
                final JsonGenerator generator = factory.createGenerator(output)) {

            generator.useDefaultPrettyPrinter();

            while (parser.nextToken() != null) {
                copyCurrentEvent(generator, parser);
            }

            generator.flush();

            return output.toString();
        } catch (final IOException e) {
            log.trace("Unable to pretty print body. Is it JSON?. Keep it as-is: `{}`", e.getMessage());
            return body;
        }
    }

    private void copyCurrentEvent(JsonGenerator generator, JsonParser parser) throws IOException {
        if (usePreciseFloats) {
            generator.copyCurrentEventExact(parser);
        } else {
            generator.copyCurrentEvent(parser);
        }
    }

}
