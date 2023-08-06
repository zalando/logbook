package org.zalando.logbook.attributes;

import lombok.AllArgsConstructor;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public final class CompositeAttributeExtractor implements AttributeExtractor {

    private final List<AttributeExtractor> attributeExtractors;

    @Nonnull
    @Override
    public HttpAttributes extract(final HttpRequest request) {
        final Map<String, String> map = new HashMap<>();
        for (final AttributeExtractor attributeExtractor : attributeExtractors) {
            map.putAll(safeRequestExtractor(attributeExtractor, request));
        }
        return new HttpAttributes(map);
    }

    @Nonnull
    @Override
    public HttpAttributes extract(final HttpRequest request, HttpResponse response) {
        final Map<String, String> map = new HashMap<>();
        for (final AttributeExtractor attributeExtractor : attributeExtractors) {
            map.putAll(safeRequestExtractor(attributeExtractor, request, response));
        }
        return new HttpAttributes(map);
    }

    @Nonnull
    private HttpAttributes safeRequestExtractor(final AttributeExtractor attributeExtractor,
                                                final HttpRequest request) {
        try {
            return attributeExtractor.extract(request);
        } catch (Exception e) {
            attributeExtractor.logException(e);
            return HttpAttributes.EMPTY;
        }
    }

    @Nonnull
    private HttpAttributes safeRequestExtractor(final AttributeExtractor attributeExtractor,
                                                final HttpRequest request,
                                                final HttpResponse response) {
        try {
            return attributeExtractor.extract(request, response);
        } catch (Exception e) {
            attributeExtractor.logException(e);
            return HttpAttributes.EMPTY;
        }
    }
}
