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

    private static final Logger LOGGER = LoggerFactory.getLogger(Logbook.class);

    private final StructuredHttpLogFormatter structuredHttpLogFormatter;

    @Override
    public void write(Precorrelation precorrelation, HttpRequest httpRequest) throws IOException {
        Map<String, Object> content = structuredHttpLogFormatter.prepare(precorrelation, httpRequest);
        write(content);
    }

    @Override
    public void write(Correlation correlation, HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        Map<String, Object> content = structuredHttpLogFormatter.prepare(correlation, httpResponse);
        write(content);
    }

    private void write(Map<String, Object> content) throws IOException {
        LoggingEventBuilder loggingEventBuilder = LOGGER.atTrace();
        content.forEach(loggingEventBuilder::addKeyValue);
        loggingEventBuilder.log(structuredHttpLogFormatter.format(content));
    }

}
