package org.zalando.logbook.logstash;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.PrettyPrinter;
import net.logstash.logback.marker.RawJsonAppendingMarker;
import org.apiguardian.api.API;
import org.slf4j.Marker;

import java.io.IOException;

import static org.apiguardian.api.API.Status.INTERNAL;

/**
 * Auto-detecting pretty-printing {@link Marker}. If pretty-printing is enabled,
 * indents using the log framework's own {@link JsonGenerator}.
 */
@API(status = INTERNAL)
final class AutodetectPrettyPrintingMarker extends RawJsonAppendingMarker {

    private static final long serialVersionUID = 1L;

    AutodetectPrettyPrintingMarker(final String fieldName, final String rawJson) {
        super(fieldName, rawJson);
    }

    @Override
    protected void writeFieldValue(final JsonGenerator generator) throws IOException {
        final PrettyPrinter prettyPrinter = generator.getPrettyPrinter();

        if (prettyPrinter == null) {
            super.writeFieldValue(generator);
        } else {
            final JsonFactory factory = generator.getCodec().getFactory();

            // append to existing tree event by event
            try (final JsonParser parser = factory.createParser(super.getFieldValue().toString())) {
                while (parser.nextToken() != null) {
                    generator.copyCurrentEvent(parser);
                }
            }
        }
    }

}
