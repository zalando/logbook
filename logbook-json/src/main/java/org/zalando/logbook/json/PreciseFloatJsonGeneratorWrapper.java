package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

final class PreciseFloatJsonGeneratorWrapper extends JsonGeneratorWrapper {

    public PreciseFloatJsonGeneratorWrapper(JsonGenerator delegate) {
        super(delegate);
    }

    @Override
    public void copyCurrentEvent(JsonParser parser) throws IOException {
        delegate.copyCurrentEventExact(parser);
    }
}
