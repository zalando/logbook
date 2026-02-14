package org.zalando.logbook.json;


import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;

import java.io.IOException;

final class PreciseFloatJsonGeneratorWrapper implements JsonGeneratorWrapper {

    @Override
    public void copyCurrentEvent(JsonGenerator delegate, JsonParser parser) {
        delegate.copyCurrentEventExact(parser);
    }
}
