package org.zalando.logbook;

import com.google.common.collect.Multimap;

import java.io.IOException;

import static com.google.common.collect.Multimaps.transformEntries;

final class ObfuscatedHttpResponse extends ForwardingHttpResponse {

    private final HttpResponse response;
    private final Obfuscator headerObfuscator;
    private final BodyObfuscator bodyObfuscator;

    ObfuscatedHttpResponse(final HttpResponse response, final Obfuscator headerObfuscator,
            final BodyObfuscator bodyObfuscator) {
        this.response = response;
        this.headerObfuscator = headerObfuscator;
        this.bodyObfuscator = bodyObfuscator;
    }

    @Override
    protected HttpResponse delegate() {
        return response;
    }

    @Override
    public Multimap<String, String> getHeaders() {
        return obfuscate(response.getHeaders(), headerObfuscator);
    }

    private Multimap<String, String> obfuscate(final Multimap<String, String> values, final Obfuscator obfuscator) {
        return transformEntries(values, obfuscator::obfuscate);
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
