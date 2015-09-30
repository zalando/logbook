package org.zalando.logbook;

@FunctionalInterface
public interface BodyObfuscator {

    String obfuscate(final String contentType, final String body);
    
    static BodyObfuscator none() {
        return (contentType, body) -> body;
    }

}
