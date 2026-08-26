package org.zalando.logbook.ecs;

import org.slf4j.Logger;
import org.slf4j.spi.LoggingEventBuilder;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.StructuredHttpLogFormatter;

import java.io.IOException;
import java.util.Map;

public final class StatusCodeBasedEcsSink extends EcsSink {

    public StatusCodeBasedEcsSink(Logger logger, StructuredHttpLogFormatter structuredHttpLogFormatter) {
        super(logger, structuredHttpLogFormatter);
    }

    public StatusCodeBasedEcsSink(StructuredHttpLogFormatter structuredHttpLogFormatter) {
        super(structuredHttpLogFormatter);
    }

    @Override
    public boolean isActive() {
        return logger.isTraceEnabled() || logger.isWarnEnabled() || logger.isErrorEnabled();
    }

    @Override
    public void write(Correlation correlation, HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        Map<String, Object> content = structuredHttpLogFormatter.prepare(correlation, httpResponse);
        LoggingEventBuilder loggingEventBuilder;

        if (httpResponse.getStatus() < 400) {
            loggingEventBuilder = logger.atTrace();
        } else if (httpResponse.getStatus() < 500) {
            loggingEventBuilder = logger.atWarn();
        } else {
            loggingEventBuilder = logger.atError();
        }
        write(content, loggingEventBuilder);
    }
}
