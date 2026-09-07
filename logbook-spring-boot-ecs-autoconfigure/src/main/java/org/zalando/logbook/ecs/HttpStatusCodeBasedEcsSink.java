package org.zalando.logbook.ecs;

import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.zalando.logbook.core.AbstractHttpStatusCodeSink;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.StructuredHttpLogFormatter;

import java.io.IOException;
import java.util.Map;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
public final class HttpStatusCodeBasedEcsSink extends AbstractHttpStatusCodeSink implements EcsSinkSupport {

    private final StructuredHttpLogFormatter structuredHttpLogFormatter;

    public HttpStatusCodeBasedEcsSink(Logger logger, StructuredHttpLogFormatter structuredHttpLogFormatter) {
        super(logger);
        this.structuredHttpLogFormatter = structuredHttpLogFormatter;
    }

    public HttpStatusCodeBasedEcsSink(StructuredHttpLogFormatter structuredHttpLogFormatter) {
        super();
        this.structuredHttpLogFormatter = structuredHttpLogFormatter;
    }

    @Override
    public void write(Precorrelation precorrelation, HttpRequest httpRequest) throws IOException {
        Map<String, Object> content = structuredHttpLogFormatter.prepare(precorrelation, httpRequest);
        write(content, logger.atTrace());
    }

    @Override
    public void write(Correlation correlation, HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        Map<String, Object> content = structuredHttpLogFormatter.prepare(correlation, httpResponse);
        write(content, getLoggingEventBuilder(httpResponse.getStatus()));
    }

    @Override
    public StructuredHttpLogFormatter getHttpLogFormatter() {
        return this.structuredHttpLogFormatter;
    }
}