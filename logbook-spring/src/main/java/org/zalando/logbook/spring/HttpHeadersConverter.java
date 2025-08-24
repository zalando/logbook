package org.zalando.logbook.spring;

import lombok.Value;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converter from Spring's HttpHeaders to Logbook's HttpHeaders because, since Spring Framework 7.0,
 * this class no longer implements the MultiValueMap contract.
 */
@Value(staticConstructor = "of")
public class HttpHeadersConverter {

    HttpHeaders headers;

    /**
     * Converts Spring's HttpHeaders to a MultiValueMap.
     *
     * @return HttpHeaders as MultiValueMap.
     */
    public Map<String, List<String>> toMultiValueMap( ) {
        return headers
                .headerSet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
