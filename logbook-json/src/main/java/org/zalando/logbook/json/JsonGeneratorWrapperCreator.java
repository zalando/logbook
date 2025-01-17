package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonFactory;

import java.io.CharArrayWriter;
import java.io.IOException;

public interface JsonGeneratorWrapperCreator {
    JsonGeneratorWrapper create(JsonFactory factory, CharArrayWriter output) throws IOException;
}
