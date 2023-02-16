package org.zalando.logbook.servlet;

import jakarta.servlet.AsyncEvent;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LogbookAsyncListenerTest {

    private final AsyncOnCompleteListener asyncOnCompleteListener = mock(AsyncOnCompleteListener.class);
    private final AsyncEvent asyncEvent = mock(AsyncEvent.class);

    private final LogbookAsyncListener logbookAsyncListener = new LogbookAsyncListener(asyncOnCompleteListener);

    @Test
    void onComplete() throws IOException {
        logbookAsyncListener.onComplete(asyncEvent);

        verify(asyncOnCompleteListener).onComplete(asyncEvent);
    }

    @Test
    void onTimeout() {
        logbookAsyncListener.onTimeout(asyncEvent);
    }

    @Test
    void onError() {
        logbookAsyncListener.onError(asyncEvent);
    }

    @Test
    void onStartAsync() {
        logbookAsyncListener.onStartAsync(asyncEvent);
    }
}
