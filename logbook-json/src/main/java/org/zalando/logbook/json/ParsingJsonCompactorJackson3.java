package org.zalando.logbook.json;

import lombok.Generated;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.ObjectWriteContext;
import tools.jackson.core.json.JsonFactory;

import java.io.CharArrayWriter;
import java.io.IOException;

@Generated
final class ParsingJsonCompactorJackson3 implements JsonCompactor {

    private final JsonFactory factory;

    private final JsonGeneratorWrapperJackson3 jsonGeneratorWrapper;

    public ParsingJsonCompactorJackson3(final JsonFactory factory, final JsonGeneratorWrapperJackson3 jsonGeneratorWrapper) {
        this.factory = factory;
        this.jsonGeneratorWrapper = jsonGeneratorWrapper;
    }

    public ParsingJsonCompactorJackson3(final JsonGeneratorWrapperJackson3 jsonGeneratorWrapper) {
        this(new JsonFactory(), jsonGeneratorWrapper);
    }

    public ParsingJsonCompactorJackson3() {
        this(new JsonFactory());
    }

    public ParsingJsonCompactorJackson3(final JsonFactory factory) {
        this(factory, new DefaultJsonGeneratorWrapperJackson3());
    }

    @Override
    public String compact(final String json) throws IOException {
        try (
                final CharArrayWriter output = new CharArrayWriter(json.length());
                final JsonParser parser = factory.createParser(ObjectReadContext.empty(), json);
                final JsonGenerator generator = factory.createGenerator(ObjectWriteContext.empty(), output)) {

            while (parser.nextToken() != null) {
                jsonGeneratorWrapper.copyCurrentEvent(generator, parser);
            }

            generator.flush();

            return output.toString();
        }
    }

}
