package org.zalando.logbook;

final class Obfuscation {

    private final Obfuscator headerObfuscator;
    private final Obfuscator parameterObfuscator;
    private final BodyObfuscator bodyObfuscator;

    Obfuscation(final Obfuscator headerObfuscator, final Obfuscator parameterObfuscator,
            final BodyObfuscator bodyObfuscator) {
        this.headerObfuscator = headerObfuscator;
        this.parameterObfuscator = parameterObfuscator;
        this.bodyObfuscator = bodyObfuscator;
    }

    HttpRequest obfuscate(final HttpRequest request) {
        return new ObfuscatedHttpRequest(request, headerObfuscator, parameterObfuscator, bodyObfuscator);
    }

    HttpResponse obfuscate(final HttpResponse response) {
        return new ObfuscatedHttpResponse(response, headerObfuscator, bodyObfuscator);
    }

}
