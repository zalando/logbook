package org.zalando.logbook;

import java.io.IOException;
import java.io.PrintStream;

public final class StreamHttpLogWriter implements HttpLogWriter {

    private final PrintStream stream;

    public StreamHttpLogWriter() {
        this(System.out);
    }

    public StreamHttpLogWriter(final PrintStream stream) {
        this.stream = stream;
    }

    @Override
    public void writeRequest(final Precorrelation<String> precorrelation) throws IOException {
        stream.println(precorrelation.getRequest());
    }

    @Override
    public void writeResponse(final Correlation<String, String> correlation) throws IOException {
        stream.println(correlation.getResponse());
    }

}
