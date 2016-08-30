package org.zalando.logbook;

@FunctionalInterface
public interface RequestObfuscator {

    HttpRequest obfuscate(final HttpRequest request);

    static RequestObfuscator none() {
        return request -> request;
    }

    static RequestObfuscator merge(final RequestObfuscator left, final RequestObfuscator right) {
        return request -> left.obfuscate(right.obfuscate(request));
    }

}
