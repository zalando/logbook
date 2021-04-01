package org.zalando.logbook.servlet;

import java.io.IOException;
import javax.servlet.AsyncEvent;

@FunctionalInterface
interface AsyncOnCompleteListener {
    void onComplete(AsyncEvent event) throws IOException;
}
