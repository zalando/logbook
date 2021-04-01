package org.zalando.logbook.servlet;

import java.io.IOException;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;

class LogbookAsyncListener implements AsyncListener {

    private final AsyncOnCompleteListener onCompleteListener;

    public LogbookAsyncListener(AsyncOnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    @Override
    public void onComplete(AsyncEvent event) throws IOException {
        onCompleteListener.onComplete(event);
    }

    @Override
    public void onTimeout(AsyncEvent event) {
    }

    @Override
    public void onError(AsyncEvent event) {
    }

    @Override
    public void onStartAsync(AsyncEvent event) {
    }
}
