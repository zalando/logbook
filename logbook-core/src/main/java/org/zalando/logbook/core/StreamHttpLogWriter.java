package org.zalando.logbook.core;

import org.apiguardian.api.API;
import org.zalando.logbook.api.Correlation;
import org.zalando.logbook.api.HttpLogWriter;
import org.zalando.logbook.api.Precorrelation;

import java.io.IOException;
import java.io.PrintStream;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public final class StreamHttpLogWriter implements HttpLogWriter {

    private final PrintStream stream;

    public StreamHttpLogWriter() {
        this(System.out);
    }

    public StreamHttpLogWriter(final PrintStream stream) {
        this.stream = stream;
    }

    @Override
    public void write(final Precorrelation precorrelation, final String request) throws IOException {
        stream.println(request);
    }

    @Override
    public void write(final Correlation correlation, final String response) throws IOException {
        stream.println(response);
    }

}
