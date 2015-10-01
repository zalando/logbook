package org.zalando.logbook;

import java.io.IOException;

public interface Correlation {

    void write(final RawHttpResponse response) throws IOException;

}
