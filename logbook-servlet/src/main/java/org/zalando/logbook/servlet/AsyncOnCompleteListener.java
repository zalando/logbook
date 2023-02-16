package org.zalando.logbook.servlet;

import jakarta.servlet.AsyncEvent;

import java.io.IOException;

@FunctionalInterface
interface AsyncOnCompleteListener {
    void onComplete(AsyncEvent event) throws IOException;
}
