package org.zalando.logbook.logstash;

import lombok.AllArgsConstructor;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.zalando.logbook.api.Correlation;
import org.zalando.logbook.api.HttpLogFormatter;
import org.zalando.logbook.api.HttpRequest;
import org.zalando.logbook.api.HttpResponse;
import org.zalando.logbook.api.Logbook;
import org.zalando.logbook.api.Precorrelation;
import org.zalando.logbook.api.Sink;

import java.io.IOException;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@AllArgsConstructor
@API(status = EXPERIMENTAL)
public final class LogstashLogbackSink implements Sink {

    private final static Logger log = LoggerFactory.getLogger(Logbook.class);

    private final HttpLogFormatter formatter;

    private final String baseField;

    public LogstashLogbackSink(final HttpLogFormatter formatter) {
        this(formatter, "http");
    }

    @Override
    public boolean isActive() {
        return log.isTraceEnabled();
    }

    @Override
    public void write(final Precorrelation precorrelation, final HttpRequest request) throws IOException {
        final Marker marker = new AutodetectPrettyPrintingMarker(baseField, formatter.format(precorrelation, request));
        log.trace(marker, requestMessage(request));
    }

    private String requestMessage(final HttpRequest request) {
        return request.getMethod() + " " + request.getRequestUri();
    }

    @Override
    public void write(final Correlation correlation, final HttpRequest request,
            final HttpResponse response) throws IOException {
        final Marker marker = new AutodetectPrettyPrintingMarker(baseField, formatter.format(correlation, response));

        log.trace(marker, responseMessage(request, response));
    }

    private String responseMessage(final HttpRequest request, final HttpResponse response) {
        final String requestUri = request.getRequestUri();
        final StringBuilder messageBuilder = new StringBuilder(64 + requestUri.length());
        messageBuilder.append(response.getStatus());
        final String reasonPhrase = response.getReasonPhrase();
        if (reasonPhrase != null) {
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
