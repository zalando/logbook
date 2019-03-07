package org.zalando.logbook.lle;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.PrettyPrinter;

import net.logstash.logback.marker.RawJsonAppendingMarker;

/**
 * 
 * Auto-detecting pretty-printing Marker. Pretty-printing cannot be performed in advance.
 *
 */

public class AutodetectPrettyPrintingMarker extends RawJsonAppendingMarker {
    
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
