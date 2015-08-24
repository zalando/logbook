package org.zalando.springframework.web.logging;

import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Map;

@Immutable
public class ResponseData {

    private final int status;
    private final Map<String, List<String>> headers;
    private final String contentType;
    private final String body;

    ResponseData(final int status, final Map<String, List<String>> headers, final String contentType, final String body) {
        this.status = status;
        this.headers = headers;
        this.contentType = contentType;
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public String getContentType() {
        return contentType;
    }

    public String getBody() {
        return body;
    }
}
