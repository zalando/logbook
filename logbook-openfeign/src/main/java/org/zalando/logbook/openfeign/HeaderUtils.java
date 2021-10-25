package org.zalando.logbook.openfeign;

import lombok.experimental.UtilityClass;
import org.zalando.logbook.HttpHeaders;

import java.util.*;

@UtilityClass
class HeaderUtils {

    /**
     * Convert Feign headers to Logbook-compatible format
     *
     * @param feignHeaders original headers
     * @return Logbook headers
     */
    HttpHeaders toLogbookHeaders(Map<String, Collection<String>> feignHeaders) {
        Map<String, List<String>> convertedHeaders = new HashMap<>();
        for (Map.Entry<String, Collection<String>> header : feignHeaders.entrySet()) {
            convertedHeaders.put(header.getKey(), new ArrayList<>(header.getValue()));
        }
        return HttpHeaders.of(convertedHeaders);
    }
}
