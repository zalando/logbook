package org.zalando.logbook;

import javax.servlet.http.HttpServletRequest;

interface Skippable {

    default boolean skip(final HttpServletRequest request) {
        return false;
    }

    default boolean skipAsyncDispatch() {
        return true;
    }

    default boolean skipErrorDispatch() {
        return true;
    }

}
