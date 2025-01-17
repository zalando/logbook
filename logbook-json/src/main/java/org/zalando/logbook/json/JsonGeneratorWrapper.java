package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

public abstract class JsonGeneratorWrapper implements Closeable, Flushable {
    protected final JsonGenerator delegate;

    public JsonGeneratorWrapper(JsonGenerator delegate) {
        this.delegate = delegate;
    }

    public void copyCurrentEvent(JsonParser parser) throws IOException {
        delegate.copyCurrentEvent(parser);
    }

    public void useDefaultPrettyPrinter() {
        delegate.useDefaultPrettyPrinter();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    public void writeString(String text) throws IOException {
        delegate.writeString(text);
    }
}
