package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonFactory;

import java.io.CharArrayWriter;
import java.io.IOException;

public final class PreciseFloatJsonGeneratorWrapperCreator implements JsonGeneratorWrapperCreator {
    public JsonGeneratorWrapper create(JsonFactory factory, CharArrayWriter output) throws IOException {
        return new PreciseFloatJsonGeneratorWrapper(factory.createGenerator(output));
    }
}
