package org.zalando.logbook.core;

import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
public final class HttpStatusCodeBasedSink extends AbstractHttpStatusCodeSink {

    private final HttpLogFormatter formatter;

    public HttpStatusCodeBasedSink(Logger logger, HttpLogFormatter formatter) {
        super(logger);
        this.formatter = formatter;
    }

    public HttpStatusCodeBasedSink(final HttpLogFormatter httpLogFormatter) {
        super();
        this.formatter = httpLogFormatter;
    }

    @Override
    public void write(Precorrelation precorrelation, HttpRequest request) throws IOException {
        logger.atTrace().log(formatter.format(precorrelation, request));
    }

    @Override
    public void write(final Correlation correlation, final HttpRequest request,
                      final HttpResponse response) throws IOException {
        final String message = formatter.format(correlation, response);
        getLoggingEventBuilder(response.getStatus()).log(message);
    }
}