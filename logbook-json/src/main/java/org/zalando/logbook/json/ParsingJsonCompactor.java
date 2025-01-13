package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.CharArrayWriter;
import java.io.IOException;

final class ParsingJsonCompactor implements JsonCompactor {

    private final JsonFactory factory;

    private final boolean usePreciseFloats;

    public ParsingJsonCompactor(final JsonFactory factory, final boolean usePreciseFloats) {
        this.factory = factory;
        this.usePreciseFloats = usePreciseFloats;
    }

    public ParsingJsonCompactor(final boolean usePreciseFloats) {
        this(new JsonFactory(), usePreciseFloats);
    }

    public ParsingJsonCompactor() {
        this(new JsonFactory());
    }

    public ParsingJsonCompactor(final JsonFactory factory) {
        this(factory, false);
    }

    @Override
    public String compact(final String json) throws IOException {
        try (
                final CharArrayWriter output = new CharArrayWriter(json.length());
                final JsonParser parser = factory.createParser(json);
                final JsonGenerator generator = factory.createGenerator(output)) {

            while (parser.nextToken() != null) {
                copyCurrentEvent(generator, parser);
            }

            generator.flush();

            return output.toString();
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
