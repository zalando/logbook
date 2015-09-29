package org.zalando.logbook;

import java.io.IOException;
import java.io.PrintStream;

public final class StreamHttpLogWriter implements HttpLogWriter {

    private final PrintStream stream;

    public StreamHttpLogWriter() {
        this(System.out);
    }

    public StreamHttpLogWriter(PrintStream stream) {
        this.stream = stream;
    }

    @Override
    public void writeRequest(String request) throws IOException {
        stream.println(request);
    }

    @Override
    public void writeResponse(String response) throws IOException {
        stream.println(response);
    }

}
