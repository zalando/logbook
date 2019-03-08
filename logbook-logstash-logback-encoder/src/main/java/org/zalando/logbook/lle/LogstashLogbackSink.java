package org.zalando.logbook.lle;

import java.io.IOException;

import org.slf4j.Marker;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.Sink;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class LogstashLogbackSink implements Sink {

    private final HttpLogFormatter formatter;
    private final LogstashLogbackHttpLogWriter logWriter;

    @Override
    public boolean isActive() {
        return logWriter.isActive();
    }

    @Override
    public void write(final Precorrelation precorrelation, final HttpRequest request) throws IOException {
        Marker marker = new AutodetectPrettyPrintingMarker("http", formatter.format(precorrelation, request));

        logWriter.write(precorrelation, marker, requestMessage(request));
    }

	protected String requestMessage(final HttpRequest request) {
		return request.getMethod() + " " + request.getRequestUri();
	}

    @Override
    public void write(final Correlation correlation, final HttpRequest request, final HttpResponse response)
            throws IOException {
        Marker marker = new AutodetectPrettyPrintingMarker("http", formatter.format(correlation, response));

        logWriter.write(correlation, marker, responseMessage(request, response));
    }

	protected String responseMessage(final HttpRequest request, final HttpResponse response) {
		return request.getMethod() + ' ' + request.getRequestUri() + ' ' + response.getStatus();
	}

}
