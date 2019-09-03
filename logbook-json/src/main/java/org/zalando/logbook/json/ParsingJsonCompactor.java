package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.CharArrayWriter;
import java.io.IOException;

final class ParsingJsonCompactor implements JsonCompactor {

    private final JsonFactory factory;

    public ParsingJsonCompactor() {
        this(new JsonFactory());
    }

    public ParsingJsonCompactor(final JsonFactory factory) {
        this.factory = factory;
    }

    @Override
    public String compact(final String json) throws IOException {
        try (
            final CharArrayWriter output = new CharArrayWriter(json.length());
            final JsonParser parser = factory.createParser(json);
            final JsonGenerator generator = factory.createGenerator(output);
                ) {

            while (parser.nextToken() != null) {
                generator.copyCurrentEvent(parser);
            }

            generator.flush();
            
            return output.toString();
        }
    }

}
