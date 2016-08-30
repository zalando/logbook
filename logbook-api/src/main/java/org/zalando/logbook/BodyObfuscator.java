package org.zalando.logbook;

@FunctionalInterface
public interface BodyObfuscator {

    String obfuscate(final String contentType, final String body);
    
    static BodyObfuscator none() {
        return (contentType, body) -> body;
    }

    static BodyObfuscator merge(final BodyObfuscator left, final BodyObfuscator right) {
        return (contentType, body) -> left.obfuscate(contentType, right.obfuscate(contentType, body));
    }

}
