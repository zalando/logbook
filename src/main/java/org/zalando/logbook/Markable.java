package org.zalando.logbook;

import javax.servlet.ServletRequest;

interface Markable extends Named {

    default boolean isMarked(final ServletRequest request) {
        return request.getAttribute(getName()) != null;
    }

    default void mark(final ServletRequest request) {
        request.setAttribute(getName(), Boolean.TRUE);
    }

    default void unmark(final ServletRequest request) {
        request.removeAttribute(getName());
    }

}
