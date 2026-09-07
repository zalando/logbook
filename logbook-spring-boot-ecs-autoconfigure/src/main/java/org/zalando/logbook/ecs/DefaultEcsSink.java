package org.zalando.logbook.ecs;

import lombok.RequiredArgsConstructor;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.StructuredHttpLogFormatter;

import java.io.IOException;
import java.util.Map;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
@RequiredArgsConstructor
public final class DefaultEcsSink implements EcsSinkSupport {

    private final Logger logger;
    private final StructuredHttpLogFormatter structuredHttpLogFormatter;

    public DefaultEcsSink(StructuredHttpLogFormatter structuredHttpLogFormatter) {
        this.logger = LoggerFactory.getLogger(Logbook.class);
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
        write(content, logger.atTrace());
    }

    @Override
    public StructuredHttpLogFormatter getHttpLogFormatter() {
        return this.structuredHttpLogFormatter;
    }
}