package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

final class PreciseFloatJsonGeneratorWrapper implements JsonGeneratorWrapper {

    @Override
    public void copyCurrentEvent(JsonGenerator delegate, JsonParser parser) throws IOException {
        delegate.copyCurrentEventExact(parser);
    }
}
