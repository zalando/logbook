package org.zalando.logbook.servlet;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * Wraps the {@link AsyncOnCompleteListener} that {@link LogbookFilter} registers for asynchronous requests.
 * Wrapping happens on the request thread, which is where thread-local context, e.g. a tracing context, has to
 * be captured in order to be available when the response is logged.
 */
@API(status = EXPERIMENTAL)
@FunctionalInterface
public interface AsyncOnCompleteListenerWrapper {

    AsyncOnCompleteListener wrap(AsyncOnCompleteListener listener);

    static AsyncOnCompleteListenerWrapper identity() {
        return listener -> listener;
    }

}
