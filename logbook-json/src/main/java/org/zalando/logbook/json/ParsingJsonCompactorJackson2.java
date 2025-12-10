package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import lombok.Generated;

import java.io.CharArrayWriter;
import java.io.IOException;

final class ParsingJsonCompactorJackson2 implements JsonCompactor {

    private final JsonFactory factory;

    private final JsonGeneratorWrapperJackson2 jsonGeneratorWrapperJackson2;

    public ParsingJsonCompactorJackson2(final JsonFactory factory, final JsonGeneratorWrapperJackson2 jsonGeneratorWrapperJackson2) {
        this.factory = factory;
        this.jsonGeneratorWrapperJackson2 = jsonGeneratorWrapperJackson2;
    }

    public ParsingJsonCompactorJackson2(final JsonGeneratorWrapperJackson2 jsonGeneratorWrapperJackson2) {
        this(new JsonFactory(), jsonGeneratorWrapperJackson2);
    }

    @Generated
    public ParsingJsonCompactorJackson2() {
        this(new JsonFactory());
    }

    @Generated
    public ParsingJsonCompactorJackson2(final JsonFactory factory) {
        this(factory, new DefaultJsonGeneratorWrapperJackson2());
    }

    @Override
    public String compact(final String json) throws IOException {
        try (
                final CharArrayWriter output = new CharArrayWriter(json.length());
                final JsonParser parser = factory.createParser(json);
                final JsonGenerator generator = factory.createGenerator(output)) {


            while (parser.nextToken() != null) {
                jsonGeneratorWrapperJackson2.copyCurrentEvent(generator, parser);
            }

            generator.flush();

            return output.toString();
        }
    }

}
