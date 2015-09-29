package org.zalando.logbook;

import java.io.IOException;

public interface HttpLogFormatter {

    String format(final TeeHttpServletRequest request) throws IOException;

    String format(final TeeHttpServletResponse response) throws IOException;

}
