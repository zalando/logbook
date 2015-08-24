package org.zalando.springframework.web.logging;

import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Map;

@Immutable
public class RequestData {

    private final String remote;
    private final String method;
    private final String url;
    private final Map<String, List<String>> headers;
    private final Map<String, List<String>> parameters;
    private final String body;

    public RequestData(final String remote, final String method, final String url, final Map<String, List<String>> headers,
            final Map<String, List<String>> parameters, final String body) {
        this.remote = remote;
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.parameters = parameters;
        this.body = body;
    }

    public String getRemote() {
        return remote;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public Map<String, List<String>> getParameters() {
        return parameters;
    }

    public String getBody() {
        return body;
    }
}
