package org.zalando.logbook;

import lombok.AllArgsConstructor;

import java.util.Collection;

import static org.zalando.fauxpas.FauxPas.throwingConsumer;

@AllArgsConstructor
public final class CompositeSink implements Sink {

    private final Collection<Sink> sinks;

    @Override
    public boolean isActive() {
        return sinks.stream().anyMatch(Sink::isActive);
    }

    @Override
    public void write(final Precorrelation precorrelation, final HttpRequest request) {
        sinks.forEach(throwingConsumer(sink -> sink.write(precorrelation, request)));
    }

    @Override
    public void write(final Correlation correlation, final HttpRequest request, final HttpResponse response) {
        sinks.forEach(throwingConsumer(sink -> sink.write(correlation, request, response)));
    }

    @Override
    public void writeBoth(final Correlation correlation, final HttpRequest request, final HttpResponse response) {
        sinks.forEach(throwingConsumer(sink -> sink.writeBoth(correlation, request, response)));
    }

}
