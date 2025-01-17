package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.json.JsonWriteFeature;

import java.io.CharArrayWriter;
import java.io.IOException;


public final class NumberAsStringJsonGeneratorWrapperCreator implements JsonGeneratorWrapperCreator {
    public JsonGeneratorWrapper create(JsonFactory factory, CharArrayWriter output) throws IOException {
        final JsonGenerator generator = factory.rebuild()
                .enable(JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS)
                .build()
                .createGenerator(output);
        return new NumberAsStringJsonGeneratorWrapper(generator);
    }
}
