package org.zalando.logbook.core;

import org.zalando.logbook.api.StructuredHttpLogFormatter;

import java.util.Map;
import java.util.stream.Collectors;

public class SplunkHttpLogFormatter implements StructuredHttpLogFormatter {

    @Override
    public String format(final Map<String, Object> content) {
        return content.entrySet().stream()
                .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(" "));
    }

}
