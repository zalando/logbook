package org.zalando.logbook;

import org.apiguardian.api.API;

import java.io.IOException;
import java.util.Optional;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface HttpRequest extends HttpMessage {

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

    // TODO void vs pseudo-function (mutable)
    HttpRequest withBody() throws IOException;

    // TODO require all implementations to be without-body by default
    HttpRequest withoutBody();

}
