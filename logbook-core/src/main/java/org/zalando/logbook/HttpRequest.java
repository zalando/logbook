package org.zalando.logbook;

import com.google.common.collect.Multimap;

public interface HttpRequest extends HttpMessage {

    String getRemote();

    String getMethod();

    /**
     * Request URI including query string.
     *
     * @return the requested URI
     */
    String getRequestURI();

    Multimap<String, String> getParameters();

}
