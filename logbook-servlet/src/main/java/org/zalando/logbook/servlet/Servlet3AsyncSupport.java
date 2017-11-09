package org.zalando.logbook.servlet;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;

public class Servlet3AsyncSupport implements AsyncSupport {

    @Override
    public boolean isFirstRequest(final HttpServletRequest request) {
        return request.getDispatcherType() != DispatcherType.ASYNC;
    }

    @Override
    public boolean isLastRequest(final HttpServletRequest request) {
        return !request.isAsyncStarted();
    }

}
