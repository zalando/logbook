package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

public interface JsonGeneratorWrapper {

    default void copyCurrentEvent(final JsonGenerator delegate, final JsonParser parser) throws IOException {
        delegate.copyCurrentEvent(parser);
    }

}
