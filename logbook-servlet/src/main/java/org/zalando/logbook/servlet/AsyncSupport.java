package org.zalando.logbook.servlet;

import javax.servlet.http.HttpServletRequest;

interface AsyncSupport {

    AsyncSupport INSTANCE = ClassPath.load("javax.servlet.DispatcherType",
            Servlet3AsyncSupport::new, Servlet25AsyncSupport::new);

    boolean isFirstRequest(HttpServletRequest request);

    boolean isLastRequest(HttpServletRequest request);

}
