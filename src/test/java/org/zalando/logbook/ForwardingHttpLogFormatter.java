package org.zalando.logbook;

import com.google.common.collect.ForwardingObject;

import java.io.IOException;

public class ForwardingHttpLogFormatter extends ForwardingObject implements HttpLogFormatter {

    private final HttpLogFormatter formatter;

    protected ForwardingHttpLogFormatter(final HttpLogFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    protected HttpLogFormatter delegate() {
        return formatter;
    }

    @Override
    public String format(final TeeHttpServletRequest request) throws IOException {
        return formatter.format(request);
    }

    @Override
    public String format(final TeeHttpServletResponse response) throws IOException {
        return formatter.format(response);
    }

}
