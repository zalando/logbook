package org.zalando.logbook;

import java.io.IOException;

@FunctionalInterface
public interface Correlator {

    void write(final RawHttpResponse response) throws IOException;

}
