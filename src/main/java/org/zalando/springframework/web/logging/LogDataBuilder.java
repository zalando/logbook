package org.zalando.springframework.web.logging;

import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.list;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class LogDataBuilder {

    private final HeaderObfuscator headerObfuscator;

    private final boolean includePayload;

    public LogDataBuilder() {
        this(new NullHeaderObfuscator(), true);
    }

    public LogDataBuilder(final boolean includePayload) {
        this(new NullHeaderObfuscator(), includePayload);
    }

    public LogDataBuilder(final HeaderObfuscator headerObfuscator) {
        this(headerObfuscator, true);
    }

    public LogDataBuilder(final HeaderObfuscator headerObfuscator, final boolean includePayload) {
        this.headerObfuscator = headerObfuscator;
        this.includePayload = includePayload;
    }

    public RequestData buildRequest(final HttpServletRequest request) {
        final String remote = request.getRemoteAddr();

        final String method = request.getMethod();

        final String uri = request.getRequestURL().toString();

        final Map<String, List<String>> headers = list(request.getHeaderNames())
                .stream()
                .collect(toMap(h -> h, h -> list(request.getHeaders(h))
                        .stream()
                        .map(v -> headerObfuscator.obfuscate(h, v))
                        .collect(toList())));

        final Map<String, List<String>> parameters =
                request.getParameterMap().entrySet().stream().collect(toMap(Map.Entry::getKey, entry -> asList(entry.getValue())));

        return new RequestData(remote, method, uri, headers, parameters, payload(request));
    }

    public ResponseData buildResponse(final HttpServletResponse response) {
        final int status = response.getStatus();

        final Map<String, List<String>> headers = (response.getHeaderNames())
                .stream()
                .collect(toMap(h -> h, h -> singletonList(headerObfuscator.obfuscate(h, response.getHeader(h)))));

        final String contentType = response.getContentType();

        return new ResponseData(status, headers, contentType, payload(response));
    }

    private String payload(final HttpServletRequest request) {
        if (!includePayload) {
            return "<payload is not included>";
        }

        if (request instanceof ConsumingHttpServletRequestWrapper) {
            return payload(((ConsumingHttpServletRequestWrapper) request).getContentAsByteArray(), request.getCharacterEncoding());
        } else {
            return "<payload is not consumable>";
        }
    }

    private String payload(final HttpServletResponse response) {
        if (!includePayload) {
            return "<payload is not included>";
        }

        if (response instanceof ContentCachingResponseWrapper) {
            return payload(((ContentCachingResponseWrapper) response).getContentAsByteArray(), response.getCharacterEncoding());
        } else {
            return "<payload is not consumable>";
        }
    }

    private String payload(final byte[] buffer, final String encoding) {
        final Charset charset;
        if (encoding != null && Charset.isSupported(encoding)) {
            charset = Charset.forName(encoding);
        } else {
            charset = Charset.defaultCharset();
        }
        return new String(buffer, 0, buffer.length, charset);
    }
}
