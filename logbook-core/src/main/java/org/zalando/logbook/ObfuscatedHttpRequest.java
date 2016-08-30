package org.zalando.logbook;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class ObfuscatedHttpRequest extends ForwardingHttpRequest {

    private final HttpRequest request;
    private final QueryObfuscator queryObfuscator;
    private final BodyObfuscator bodyObfuscator;
    private final Map<String, List<String>> headers;

    ObfuscatedHttpRequest(final HttpRequest request,
            final QueryObfuscator queryObfuscator,
            final HeaderObfuscator headerObfuscator,
            final BodyObfuscator bodyObfuscator) {
        this.request = request;
        this.queryObfuscator = queryObfuscator;
        this.bodyObfuscator = bodyObfuscator;
        this.headers = Obfuscators.obfuscateHeaders(request.getHeaders(), headerObfuscator::obfuscate);
    }

    @Override
    protected HttpRequest delegate() {
        return request;
    }

    @Override
    public String getRequestUri() {
        return RequestURI.reconstruct(this);
    }

    @Override
    public String getQuery() {
        final String query = super.getQuery();
        return query.isEmpty() ? query : queryObfuscator.obfuscate(query);
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    @Override
    public byte[] getBody() throws IOException {
        return getBodyAsString().getBytes(getCharset());
    }

    @Override
    public String getBodyAsString() throws IOException {
        return bodyObfuscator.obfuscate(getContentType(), request.getBodyAsString());
    }

}
