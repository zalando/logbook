package org.zalando.logbook;

import org.apiguardian.api.API;

import java.io.IOException;
import java.util.Optional;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface Logbook {

    Optional<Correlator> write(final RawHttpRequest request) throws IOException;

    static Logbook create() {
        return builder().build();
    }

    static LogbookCreator.Builder builder() {
        return LogbookCreator.builder();
    }

}
