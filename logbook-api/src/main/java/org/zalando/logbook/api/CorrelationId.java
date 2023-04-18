package org.zalando.logbook.api;

public interface CorrelationId {
    String generate(HttpRequest request);
}
