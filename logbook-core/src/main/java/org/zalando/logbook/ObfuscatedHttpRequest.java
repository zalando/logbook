package org.zalando.logbook;

import com.google.common.collect.Multimap;

import java.io.IOException;

import static com.google.common.collect.Multimaps.transformEntries;

final class ObfuscatedHttpRequest extends ForwardingHttpRequest {

    private final HttpRequest request;
    private final Obfuscator headerObfuscator;
    private final Obfuscator parameterObfuscator;
    private final BodyObfuscator bodyObfuscator;

    ObfuscatedHttpRequest(final HttpRequest request, final Obfuscator headerObfuscator,
            final Obfuscator parameterObfuscator, final BodyObfuscator bodyObfuscator) {
        this.request = request;
        this.headerObfuscator = headerObfuscator;
        this.parameterObfuscator = parameterObfuscator;
        this.bodyObfuscator = bodyObfuscator;
    }

    @Override
    protected HttpRequest delegate() {
        return request;
    }

    @Override
    public Multimap<String, String> getParameters() {
        return obfuscate(request.getParameters(), parameterObfuscator);
    }

    @Override
    public Multimap<String, String> getHeaders() {
        return obfuscate(request.getHeaders(), headerObfuscator);
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
        return bodyObfuscator.obfuscate(request.getContentType(), request.getBodyAsString());
    }

}
