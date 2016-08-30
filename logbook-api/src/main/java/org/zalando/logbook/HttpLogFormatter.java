package org.zalando.logbook;

import java.io.IOException;

public interface HttpLogFormatter {

    String format(final Precorrelation<HttpRequest> precorrelation) throws IOException;

    String format(final Correlation<HttpRequest, HttpResponse> correlation) throws IOException;

}
