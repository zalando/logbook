package org.zalando.logbook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO make level configurable?
public final class DefaultHttpLogWriter implements HttpLogWriter {

    private final Logger logger;

    public DefaultHttpLogWriter() {
        this(LoggerFactory.getLogger("logbook"));
    }

    public DefaultHttpLogWriter(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean isActive(final HttpServletRequest request, final HttpServletResponse response) {
        return logger.isTraceEnabled();
    }

    @Override
    public void writeRequest(final String request) {
        logger.trace("{}", request);
    }

    @Override
    public void writeResponse(final String response) {
        logger.trace("{}", response);
    }
}
