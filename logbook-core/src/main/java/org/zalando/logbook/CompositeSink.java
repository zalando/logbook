package org.zalando.logbook;

import lombok.AllArgsConstructor;
import org.zalando.fauxpas.ThrowingConsumer;

import java.io.IOException;
import java.util.Collection;

@AllArgsConstructor
public final class CompositeSink implements Sink {

    private final Collection<Sink> sinks;

    @Override
    public boolean isActive() {
        return sinks.stream().anyMatch(Sink::isActive);
    }

    @Override
    public void write(final Precorrelation precorrelation, final HttpRequest request) {
        each(sink -> sink.write(precorrelation, request));
    }

    @Override
    public void write(final Correlation correlation, final HttpRequest request, final HttpResponse response) {
        each(sink -> sink.write(correlation, request, response));
    }

    @Override
    public void writeBoth(final Correlation correlation, final HttpRequest request, final HttpResponse response) {
        each(sink -> sink.writeBoth(correlation, request, response));
    }

    private void each(final ThrowingConsumer<Sink, IOException> consumer) {
        sinks.stream().filter(Sink::isActive).forEach(consumer);
    }

}
