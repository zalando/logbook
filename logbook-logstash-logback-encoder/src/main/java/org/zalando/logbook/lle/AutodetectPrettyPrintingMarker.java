package org.zalando.logbook.lle;

import java.io.IOException;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.PrettyPrinter;

import net.logstash.logback.marker.RawJsonAppendingMarker;

/**
 * 
 * Auto-detecting pretty-printing {@linkplain Marker}. If pretty-printing is enabled,
 * indents using the log framework's own {@linkplain JsonGenerator}. 
 *
 */

@API(status=INTERNAL)
public final class AutodetectPrettyPrintingMarker extends RawJsonAppendingMarker {
    
    private static final long serialVersionUID = 1L;

    public AutodetectPrettyPrintingMarker(String fieldName, String rawJson) {
        super(fieldName, rawJson);
    }

    @Override
    protected void writeFieldValue(JsonGenerator generator) throws IOException {
        PrettyPrinter prettyPrinter = generator.getPrettyPrinter();
        if(prettyPrinter == null) {
            super.writeFieldValue(generator);
        } else {
            // append to existing tree event by event
            final JsonParser parser = generator.getCodec().getFactory().createParser((String)super.getFieldValue());

            try {
                while (parser.nextToken() != null) {
                    generator.copyCurrentEvent(parser);
                }
            } finally {
                parser.close();
            }
        }
    }


}
