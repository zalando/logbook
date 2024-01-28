package org.zalando.logbook.logstash;

import lombok.AllArgsConstructor;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.Sink;

import java.io.IOException;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@AllArgsConstructor
@API(status = EXPERIMENTAL)
public final class LogstashLogbackSink implements Sink {

    private final static Logger log = LoggerFactory.getLogger(Logbook.class);

    private final HttpLogFormatter formatter;

    private final String baseField;

    private final Level level;

    public LogstashLogbackSink(final HttpLogFormatter formatter) {
        this(formatter, "http",  Level.TRACE);
    }

    public LogstashLogbackSink(final HttpLogFormatter formatter, Level level) {
        this(formatter, "http",  level);
    }

    @Override
    public boolean isActive() {
        return log.isEnabledForLevel(level);
    }

    @Override
    public void write(final Precorrelation precorrelation, final HttpRequest request) throws IOException {
        final Marker marker = new AutodetectPrettyPrintingMarker(baseField, formatter.format(precorrelation, request));
        log.atLevel(level).addMarker(marker).log( requestMessage(request) );
    }

    private String requestMessage(final HttpRequest request) {
        return request.getMethod() + " " + request.getRequestUri();
    }

    @Override
    public void write(final Correlation correlation, final HttpRequest request,
            final HttpResponse response) throws IOException {
        final Marker marker = new AutodetectPrettyPrintingMarker(baseField, formatter.format(correlation, response));

        log.atLevel(level).addMarker(marker).log( responseMessage(request, response) );
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
