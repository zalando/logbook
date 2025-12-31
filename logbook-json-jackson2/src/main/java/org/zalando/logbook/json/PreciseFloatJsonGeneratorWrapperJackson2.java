package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

@Deprecated(since = "4.0.0", forRemoval = true)
final class PreciseFloatJsonGeneratorWrapperJackson2 implements JsonGeneratorWrapperJackson2 {

    @Override
    public void copyCurrentEvent(JsonGenerator delegate, JsonParser parser) throws IOException {
        delegate.copyCurrentEventExact(parser);
    }
}
