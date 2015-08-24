package org.zalando.springframework.web.logging;

public interface HeaderObfuscator {

    String obfuscate(final String headerName, final String headerValue);

}
