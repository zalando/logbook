package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.CharArrayWriter;
import java.io.IOException;

final class ParsingJsonCompactor implements JsonCompactor {

    private final JsonFactory factory;

    private final JsonGeneratorWrapper jsonGeneratorWrapper;

    public ParsingJsonCompactor(final JsonFactory factory, final JsonGeneratorWrapper jsonGeneratorWrapper) {
        this.factory = factory;
        this.jsonGeneratorWrapper = jsonGeneratorWrapper;
    }

    public ParsingJsonCompactor(final JsonGeneratorWrapper jsonGeneratorWrapper) {
        this(new JsonFactory(), jsonGeneratorWrapper);
    }

    public ParsingJsonCompactor() {
        this(new JsonFactory());
    }

    public ParsingJsonCompactor(final JsonFactory factory) {
        this(factory, new DefaultJsonGeneratorWrapper());
    }

    @Override
    public String compact(final String json) throws IOException {
        try (
                final CharArrayWriter output = new CharArrayWriter(json.length());
                final JsonParser parser = factory.createParser(json);
                final JsonGenerator generator = factory.createGenerator(output)) {


            while (parser.nextToken() != null) {
                jsonGeneratorWrapper.copyCurrentEvent(generator, parser);
            }

            generator.flush();

            return output.toString();
        }
    }

}
