package org.zalando.logbook;

import org.apiguardian.api.API;

import java.io.IOException;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface Logbook {

    RequestWritingStage process(HttpRequest request) throws IOException;
    RequestWritingStage process(HttpRequest request, Strategy strategy) throws IOException;

    interface RequestWritingStage extends ResponseProcessingStage {
        ResponseProcessingStage write() throws IOException;
    }

    interface ResponseProcessingStage {
        ResponseWritingStage process(HttpResponse response) throws IOException;
    }

    interface ResponseWritingStage {
        void write() throws IOException;
    }

    static Logbook create() {
        return builder().build();
    }

    static LogbookCreator.Builder builder() {
        return LogbookCreator.builder();
    }

}
