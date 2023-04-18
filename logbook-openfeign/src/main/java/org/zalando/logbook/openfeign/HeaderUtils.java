package org.zalando.logbook.openfeign;

import org.zalando.logbook.api.HttpHeaders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class HeaderUtils {
    private HeaderUtils() {
    }

    /**
     * Convert Feign headers to Logbook-compatible format
     *
     * @param feignHeaders original headers
     * @return Logbook headers
     */
    static HttpHeaders toLogbookHeaders(Map<String, Collection<String>> feignHeaders) {
        Map<String, List<String>> convertedHeaders = new HashMap<>();
        for (Map.Entry<String, Collection<String>> header : feignHeaders.entrySet()) {
            convertedHeaders.put(header.getKey(), new ArrayList<>(header.getValue()));
        }
        return HttpHeaders.of(convertedHeaders);
    }
}
