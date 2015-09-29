package org.zalando.logbook;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ServletRequest;

interface DispatchAware {

    default boolean isAsyncDispatch(final ServletRequest request) {
        return request.getAsyncContext() != null;
    }

    default boolean isErrorDispatch(final ServletRequest request) {
        return request.getAttribute("javax.servlet.error.request_uri") != null; // TODO constant?
    }

}
