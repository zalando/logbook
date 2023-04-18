package org.zalando.logbook.core;

import org.zalando.logbook.api.HttpResponse;
import org.zalando.logbook.api.Logbook.RequestWritingStage;

import static org.zalando.logbook.api.Logbook.ResponseProcessingStage;
import static org.zalando.logbook.api.Logbook.ResponseWritingStage;

final class Stages {

    private Stages() {

    }

    static RequestWritingStage noop() {
        return NoopRequestWriting.INSTANCE;
    }

    private enum NoopRequestWriting implements RequestWritingStage, Noop {

        INSTANCE;

        @Override
        public ResponseProcessingStage write() {
            return NoopResponseProcessing.INSTANCE;
        }

    }

    private enum NoopResponseProcessing implements Noop {
        INSTANCE
    }

    private interface Noop extends ResponseProcessingStage {

        @Override
        default ResponseWritingStage process(final HttpResponse response) {
            return NoopResponseWriting.INSTANCE;
        }

    }

    private enum NoopResponseWriting implements ResponseWritingStage {

        INSTANCE;

        @Override
        public void write() {
            // nothing to do here
        }

    }

}
