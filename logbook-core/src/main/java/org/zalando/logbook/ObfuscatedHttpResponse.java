package org.zalando.logbook;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class ObfuscatedHttpResponse extends ForwardingHttpResponse {

    private final HttpResponse response;
    private final BodyObfuscator bodyObfuscator;
    private final Map<String, List<String>> headers;

    ObfuscatedHttpResponse(final HttpResponse response, final HeaderObfuscator headerObfuscator,
            final BodyObfuscator bodyObfuscator) {
        this.response = response;
        this.bodyObfuscator = bodyObfuscator;
        this.headers = Obfuscators.obfuscateHeaders(response.getHeaders(), headerObfuscator::obfuscate);
    }

    @Override
    protected HttpResponse delegate() {
        return response;
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
        return bodyObfuscator.obfuscate(response.getContentType(), response.getBodyAsString());
    }

}
