package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

@Deprecated(since = "4.0.0", forRemoval = true)
public interface JsonGeneratorWrapperJackson2 {

    default void copyCurrentEvent(final JsonGenerator delegate, final JsonParser parser) throws IOException {
        delegate.copyCurrentEvent(parser);
    }

}
