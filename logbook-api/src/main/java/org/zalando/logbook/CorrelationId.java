package org.zalando.logbook;

public interface CorrelationId {
    String generate(HttpRequest request);
}
