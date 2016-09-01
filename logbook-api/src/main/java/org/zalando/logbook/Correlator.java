package org.zalando.logbook;

import java.io.IOException;

public interface Correlator {

    void write(final RawHttpResponse response) throws IOException;

}
