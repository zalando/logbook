package org.zalando.logbook;

@FunctionalInterface
public interface ResponseObfuscator {

    HttpResponse obfuscate(final HttpResponse response);

    static ResponseObfuscator none() {
        return response -> response;
    }

    static ResponseObfuscator merge(final ResponseObfuscator left, final ResponseObfuscator right) {
        return response -> left.obfuscate(right.obfuscate(response));
    }

}
