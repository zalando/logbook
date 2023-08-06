package org.zalando.logbook.attributes;

import lombok.AllArgsConstructor;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public final class CompositeAttributeExtractor implements AttributeExtractor {

    private final Collection<AttributeExtractor> attributeExtractors;

    @Override
    public HttpAttributes extract(final HttpRequest request) throws Exception {
        final Map<String, String> map = new HashMap<>();
        for (final AttributeExtractor attributeExtractor : attributeExtractors) {
            map.putAll(attributeExtractor.extract(request).getMap());
        }
        return new HttpAttributes(map);
    }

    @Override
    public HttpAttributes extract(final HttpRequest request, HttpResponse response) throws Exception {
        final Map<String, String> map = new HashMap<>();
        for (final AttributeExtractor attributeExtractor : attributeExtractors) {
            map.putAll(attributeExtractor.extract(request, response).getMap());
        }
        return new HttpAttributes(map);
    }
}
