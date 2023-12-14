package org.zalando.logbook.logstash;

import lombok.AllArgsConstructor;
import org.apiguardian.api.API;
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

import java.io.IOException;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@AllArgsConstructor
@API(status = EXPERIMENTAL)
public final class LogstashLogbackSink implements Sink {

    private final static Logger log = LoggerFactory.getLogger(Logbook.class);

    private final HttpLogFormatter formatter;

    private final String baseField;

    private final String level;

    public LogstashLogbackSink(final HttpLogFormatter formatter) {
        this(formatter, "http",  "trace");
    }

    @Override
    public boolean isActive() {
        boolean active;
        switch (Level.valueOf(level)){
            case trace:
                active = log.isTraceEnabled();
                break;
            case info:
                active = log.isInfoEnabled();
                break;
            case debug:
                active = log.isDebugEnabled();
                break;
            case warn:
                active = log.isWarnEnabled();
                break;
            case error:
                active = log.isErrorEnabled();
                break;
            default:
                throw new IllegalArgumentException("the log level is unknown; it must be trace, debug, info, warn, or error");
        }
        return active;
    }

    @Override
    public void write(final Precorrelation precorrelation, final HttpRequest request) throws IOException {
        final Marker marker = new AutodetectPrettyPrintingMarker(baseField, formatter.format(precorrelation, request));
        log(marker, requestMessage(request));
    }

    private String requestMessage(final HttpRequest request) {
        return request.getMethod() + " " + request.getRequestUri();
    }

    @Override
    public void write(final Correlation correlation, final HttpRequest request,
            final HttpResponse response) throws IOException {
        final Marker marker = new AutodetectPrettyPrintingMarker(baseField, formatter.format(correlation, response));

        log(marker, responseMessage(request, response));
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

    private void log(Marker marker, String message){
        switch (Level.valueOf(level)){
            case trace:
                log.trace(marker, message);
                break;
            case info:
                log.info(marker, message);
                break;
            case debug:
                log.debug(marker, message);
                break;
            case warn:
                log.warn(marker, message);
                break;
            case error:
                log.error(marker, message);
                break;
            default:
                throw new IllegalArgumentException("the log level is unknown; it must be info, warn, or error");
        }
    }

    enum Level {
        trace,
        debug,
        info,
        warn,
        error
    }

}
