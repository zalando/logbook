package org.zalando.logbook;

import java.io.IOException;
import java.util.Optional;

public interface Logbook {

    Optional<Correlator> write(final RawHttpRequest request) throws IOException;

    static Logbook create() {
        return builder().build();
    }

    static LogbookCreator.Builder builder() {
        return LogbookCreator.builder();
    }

}
