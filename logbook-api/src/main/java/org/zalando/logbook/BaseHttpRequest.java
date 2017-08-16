package org.zalando.logbook;

import java.util.Optional;

public interface BaseHttpRequest extends BaseHttpMessage {

    String getRemote();

    String getMethod();

    /**
     * Absolute Request URI including scheme, host, port (unless http/80 or https/443), path and query string.
     *
     * <p>Note that the URI may be invalid if the client issued an HTTP request using a malformed URL.</p>
     *
     * @return the requested URI
     */
    default String getRequestUri() {
        return RequestURI.reconstruct(this);
    }

    String getScheme();

    String getHost();

    Optional<Integer> getPort();

    String getPath();

    String getQuery();

}
