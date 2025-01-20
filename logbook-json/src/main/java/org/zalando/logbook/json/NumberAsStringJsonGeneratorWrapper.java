package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

final class NumberAsStringJsonGeneratorWrapper implements JsonGeneratorWrapper {

    public void copyCurrentEvent(JsonGenerator delegate, JsonParser parser) throws IOException {
        if (parser.getCurrentToken() == JsonToken.VALUE_NUMBER_FLOAT) {
            delegate.writeString(parser.getValueAsString());
        } else {
            delegate.copyCurrentEvent(parser);
        }
    }
}
