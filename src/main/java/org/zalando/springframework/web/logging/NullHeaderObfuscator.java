package org.zalando.springframework.web.logging;

class NullHeaderObfuscator implements HeaderObfuscator {


    @Override
    public String obfuscate(final String headerName, final String headerValue) {
        return headerValue;
    }
}
