package org.zalando.logbook.servlet;

import jakarta.servlet.AsyncEvent;
import org.apiguardian.api.API;

import java.io.IOException;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
@FunctionalInterface
public interface AsyncOnCompleteListener {
    void onComplete(AsyncEvent event) throws IOException;
}
