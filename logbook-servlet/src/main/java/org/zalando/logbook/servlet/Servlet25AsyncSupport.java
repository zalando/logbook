package org.zalando.logbook.servlet;

import javax.servlet.http.HttpServletRequest;

public class Servlet25AsyncSupport implements AsyncSupport {

    @Override
    public boolean isFirstRequest(final HttpServletRequest request) {
        return true;
    }

    @Override
    public boolean isLastRequest(final HttpServletRequest request) {
        return true;
    }

}
