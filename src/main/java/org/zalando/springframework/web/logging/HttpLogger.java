package org.zalando.springframework.web.logging;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HttpLogger {

    boolean shouldLog(final HttpServletRequest request, final HttpServletResponse response);

    void logRequest(final RequestData request);

    void logResponse(final ResponseData response);

}

