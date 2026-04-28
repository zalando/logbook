package org.zalando.logbook.core;

import lombok.RequiredArgsConstructor;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
@RequiredArgsConstructor
public final class LevelBasedHttpLogWriter implements HttpLogWriter {

    private static final Logger log = LoggerFactory.getLogger(Logbook.class);

    private final Level level;

    @Override
    public boolean isActive() {
        return log.isEnabledForLevel(level);
    }

    @Override
    public void write(final Precorrelation precorrelation, final String request) {
        log.atLevel(level).log(request);
    }

    @Override
    public void write(final Correlation correlation, final String response) {
        log.atLevel(level).log(response);
    }

}
