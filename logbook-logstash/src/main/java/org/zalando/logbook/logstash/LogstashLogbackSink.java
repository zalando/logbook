package org.zalando.logbook.logstash;

import java.io.IOException;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.Sink;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@API(status=EXPERIMENTAL)
public final class LogstashLogbackSink implements Sink {

    private final static Logger log = LoggerFactory.getLogger(Logbook.class);

    private final HttpLogFormatter formatter;

    @Override
    public boolean isActive() {
        return log.isTraceEnabled();
    }

    @Override
    public void write(final Precorrelation precorrelation, final HttpRequest request) throws IOException {
        Marker marker = new AutodetectPrettyPrintingMarker("http", formatter.format(precorrelation, request));

        log.trace(marker, requestMessage(request));
    }

    private String requestMessage(final HttpRequest request) {
        return request.getMethod() + " " + request.getRequestUri();
    }

    @Override
    public void write(final Correlation correlation, final HttpRequest request, final HttpResponse response) throws IOException {
        Marker marker = new AutodetectPrettyPrintingMarker("http", formatter.format(correlation, response));

        log.trace(marker, responseMessage(request, response));
    }

    private String responseMessage(final HttpRequest request, final HttpResponse response) {
        String requestUri = request.getRequestUri();
        final StringBuilder messageBuilder = new StringBuilder(64 + requestUri.length());
        messageBuilder.append(response.getStatus());
        final String reasonPhrase = response.getReasonPhrase();
        if(reasonPhrase != null) {
            messageBuilder.append(' ');
            messageBuilder.append(reasonPhrase);
        }
        messageBuilder.append(' ');
        messageBuilder.append(request.getMethod());
        messageBuilder.append(' ');
        messageBuilder.append(requestUri);

        return messageBuilder.toString();
    }

}
