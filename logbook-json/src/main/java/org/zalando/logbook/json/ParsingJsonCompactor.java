package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

import java.io.CharArrayWriter;
import java.io.IOException;

final class ParsingJsonCompactor implements JsonCompactor {

    private final JsonFactory factory;

    private final JsonGeneratorWrapperCreator jsonGeneratorWrapperCreator;

    public ParsingJsonCompactor(final JsonFactory factory, final JsonGeneratorWrapperCreator jsonGeneratorWrapperCreator) {
        this.factory = factory;
        this.jsonGeneratorWrapperCreator = jsonGeneratorWrapperCreator;
    }

    public ParsingJsonCompactor(final JsonGeneratorWrapperCreator jsonGeneratorWrapperCreator) {
        this(new JsonFactory(), jsonGeneratorWrapperCreator);
    }

    public ParsingJsonCompactor() {
        this(new JsonFactory());
    }

    public ParsingJsonCompactor(final JsonFactory factory) {
        this(factory, new DefaultJsonGeneratorWrapperCreator());
    }

    @Override
    public String compact(final String json) throws IOException {
        try (
                final CharArrayWriter output = new CharArrayWriter(json.length());
                final JsonParser parser = factory.createParser(json);
                final JsonGeneratorWrapper generator = jsonGeneratorWrapperCreator.create(factory, output)) {

            while (parser.nextToken() != null) {
                generator.copyCurrentEvent(parser);
            }

            generator.flush();

            return output.toString();
        }
    }

}
