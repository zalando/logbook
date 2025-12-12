package org.zalando.logbook.json;


import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;

import java.io.IOException;

final class NumberAsStringJsonGeneratorWrapper implements JsonGeneratorWrapper {

    public void copyCurrentEvent(JsonGenerator delegate, JsonParser parser) throws IOException {
        if (parser.currentToken() == JsonToken.VALUE_NUMBER_FLOAT) {
            delegate.writeString(parser.getValueAsString());
        } else {
            delegate.copyCurrentEvent(parser);
        }
    }
}
