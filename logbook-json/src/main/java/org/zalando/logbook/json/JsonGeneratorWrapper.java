package org.zalando.logbook.json;

import lombok.Generated;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;

import java.io.IOException;

@Generated
public interface JsonGeneratorWrapper {

    default void copyCurrentEvent(final JsonGenerator delegate, final JsonParser parser) {
        delegate.copyCurrentEvent(parser);
    }

}
