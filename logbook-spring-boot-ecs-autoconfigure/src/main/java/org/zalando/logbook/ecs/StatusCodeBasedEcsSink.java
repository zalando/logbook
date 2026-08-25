package org.zalando.logbook.ecs;

import org.slf4j.spi.LoggingEventBuilder;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.StructuredHttpLogFormatter;

import java.io.IOException;
import java.util.Map;

public final class StatusCodeBasedEcsSink extends EcsSink {

    public StatusCodeBasedEcsSink(StructuredHttpLogFormatter structuredHttpLogFormatter) {
        super(structuredHttpLogFormatter);
    }

    @Override
    public boolean isActive() {
        return LOGGER.isTraceEnabled() || LOGGER.isWarnEnabled() || LOGGER.isErrorEnabled();
    }

    @Override
    public void write(Correlation correlation, HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        Map<String, Object> content = structuredHttpLogFormatter.prepare(correlation, httpResponse);
        LoggingEventBuilder loggingEventBuilder;

        if (httpResponse.getStatus() < 400) {
            loggingEventBuilder = LOGGER.atTrace();
        } else if (httpResponse.getStatus() < 500) {
            loggingEventBuilder = LOGGER.atWarn();
        } else {
            loggingEventBuilder = LOGGER.atError();
        }
        write(content, loggingEventBuilder);
    }
}
