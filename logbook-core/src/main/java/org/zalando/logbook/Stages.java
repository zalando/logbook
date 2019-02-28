package org.zalando.logbook;

import org.zalando.logbook.Logbook.RequestWritingStage;

final class Stages {

    private static final Logbook.ResponseWritingStage WRITE_RESPONSE = () -> {};
    private static final Logbook.ResponseProcessingStage PROCESS_RESPONSE = response -> WRITE_RESPONSE;
    private static final RequestWritingStage WRITE_REQUEST = () -> PROCESS_RESPONSE;

    private Stages() {

    }

    static RequestWritingStage noop() {
        return WRITE_REQUEST;
    }

}
