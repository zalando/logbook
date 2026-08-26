package org.zalando.logbook.ecs;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggingEventBuilder;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.Sink;
import org.zalando.logbook.StructuredHttpLogFormatter;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
public class EcsSink implements Sink {

    protected final Logger logger;
    protected final StructuredHttpLogFormatter structuredHttpLogFormatter;

    public EcsSink(StructuredHttpLogFormatter structuredHttpLogFormatter) {
        this(LoggerFactory.getLogger(Logbook.class), structuredHttpLogFormatter);
    }

    @Override
    public void write(Precorrelation precorrelation, HttpRequest httpRequest) throws IOException {
        Map<String, Object> content = structuredHttpLogFormatter.prepare(precorrelation, httpRequest);
        write(content, logger.atTrace());
    }

    @Override
    public void write(Correlation correlation, HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        Map<String, Object> content = structuredHttpLogFormatter.prepare(correlation, httpResponse);
        write(content, logger.atTrace());
    }

    protected void write(Map<String, Object> content, LoggingEventBuilder loggingEventBuilder) throws IOException {
        content.forEach(loggingEventBuilder::addKeyValue);
        loggingEventBuilder.log(structuredHttpLogFormatter.format(content));
    }
}
