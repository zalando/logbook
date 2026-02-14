package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import lombok.Generated;

import java.io.CharArrayWriter;
import java.io.IOException;

@Deprecated(since = "4.0.0", forRemoval = true)
final class ParsingJsonCompactorJackson2 implements JsonCompactorJackson2 {

    private final JsonFactory factory;

    private final JsonGeneratorWrapperJackson2 jsonGeneratorWrapper;

    public ParsingJsonCompactorJackson2(final JsonFactory factory, final JsonGeneratorWrapperJackson2 jsonGeneratorWrapper) {
        this.factory = factory;
        this.jsonGeneratorWrapper = jsonGeneratorWrapper;
    }

    public ParsingJsonCompactorJackson2(final JsonGeneratorWrapperJackson2 jsonGeneratorWrapper) {
        this(new JsonFactory(), jsonGeneratorWrapper);
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
                jsonGeneratorWrapper.copyCurrentEvent(generator, parser);
            }

            generator.flush();

            return output.toString();
        }
    }

}
