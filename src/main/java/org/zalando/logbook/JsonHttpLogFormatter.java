package org.zalando.logbook;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;

import static org.zalando.logbook.Formatting.getHeaders;

public final class JsonHttpLogFormatter implements HttpLogFormatter {

    private final ObjectMapper mapper;

    public JsonHttpLogFormatter() {
        this(new ObjectMapper()
                .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
    }

    public JsonHttpLogFormatter(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String format(final TeeHttpServletRequest request) throws IOException {
        final ImmutableMap<String, Object> content = ImmutableMap.<String, Object>builder()
                .put("sender", request.getRemoteAddr())
                .put("method", request.getMethod())
                .put("path", request.getRequestURI())
                .put("headers", getHeaders(request))
                .put("params", request.getParameterMap())
                .put("body", request.getBodyAsString())
                .build();

        return mapper.writeValueAsString(content);
    }

    @Override
    public String format(final TeeHttpServletResponse response) throws IOException {
        final ImmutableMap<String, Object> content = ImmutableMap.<String, Object>builder()
                .put("status", response.getStatus())
                .put("headers", getHeaders(response))
                .put("body", response.getBodyAsString())
                .build();

        return mapper.writeValueAsString(content);
    }

}
