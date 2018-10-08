package org.zalando.logbook;

import java.util.Map;
import java.util.stream.Collectors;

public class KeyValueHttpLogFormatter implements PreparedHttpLogFormatter {

    @Override
    public String format(final Map<String, Object> content) {
        return content.entrySet().stream()
                .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(" "));
    }

}
