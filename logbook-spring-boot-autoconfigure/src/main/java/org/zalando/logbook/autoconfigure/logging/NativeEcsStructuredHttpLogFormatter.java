package org.zalando.logbook.autoconfigure.logging;

import lombok.RequiredArgsConstructor;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.StructuredHttpLogFormatterSupport;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.StructuredHttpLogFormatter;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
public class NativeEcsStructuredHttpLogFormatter implements StructuredHttpLogFormatter {

    final static ThreadLocal<Map<String, Object>> ECS_STRUCTURED_MEMBERS = ThreadLocal.withInitial(Map::of);

    private final StructuredHttpLogFormatterSupport structuredHttpLogFormatterSupport;

    @Override
    public Map<String, Object> prepare(Precorrelation precorrelation, HttpRequest httpRequest) throws IOException {
        return structuredHttpLogFormatterSupport.resolveMembers(precorrelation, httpRequest);
    }

    @Override
    public Map<String, Object> prepare(Correlation correlation, HttpResponse httpResponse) throws IOException {
        return structuredHttpLogFormatterSupport.resolveMembers(correlation, httpResponse);
    }

    @Override
    public String format(Map<String, Object> members) throws IOException {
        ECS_STRUCTURED_MEMBERS.set(members);
        return structuredHttpLogFormatterSupport.format(members);
    }

}
