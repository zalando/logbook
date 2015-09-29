package org.zalando.logbook;

import javax.servlet.http.HttpServletRequest;

public interface TeeHttpServletRequest extends HttpServletRequest, ReadableBody {

}
