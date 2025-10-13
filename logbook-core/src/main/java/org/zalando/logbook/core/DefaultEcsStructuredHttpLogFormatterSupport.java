package org.zalando.logbook.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.StructuredHttpLogFormatter;
import org.zalando.logbook.StructuredHttpLogFormatterSupport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class DefaultEcsStructuredHttpLogFormatterSupport implements StructuredHttpLogFormatterSupport {

    private final StructuredHttpLogFormatter structuredHttpLogFormatter = content -> null;

    @Override
    public Map<String, Object> resolveMembers(Precorrelation precorrelation, HttpRequest httpRequest) throws IOException {
        Map<String, Object> requestMap = new LinkedHashMap<>();
        requestMap.put("id", precorrelation.getId());
        requestMap.put("method", httpRequest.getMethod());

        structuredHttpLogFormatter.prepareBody(httpRequest).map(Object::toString).ifPresent(body -> requestMap.put("body", Map.of("content", body, "bytes", body.getBytes(StandardCharsets.UTF_8).length)));

        Map<String, Object> httpMap = new LinkedHashMap<>();
        httpMap.put("version", resolveHttpVersion(httpRequest.getProtocolVersion()));
        httpMap.put("request", requestMap);

        Map<String, Object> urlMap = new LinkedHashMap<>();
        Optional.ofNullable(httpRequest.getRequestUri()).ifPresent(full -> urlMap.put("full", full));
        Optional.ofNullable(httpRequest.getScheme()).ifPresent(scheme -> urlMap.put("scheme", scheme));
        Optional.ofNullable(httpRequest.getHost()).ifPresent(domain -> urlMap.put("domain", domain));
        Optional.ofNullable(httpRequest.getPath()).ifPresent(path -> urlMap.put("path", path));
        Optional.ofNullable(httpRequest.getQuery()).filter(query -> !query.isEmpty()).ifPresent(query -> urlMap.put("query", query));
        resolvePort(httpRequest).ifPresent(port -> urlMap.put("port", port));

        Map<String, Object> eventMap = new LinkedHashMap<>();
        eventMap.put("kind", "event");
        eventMap.put("category", "web");
        eventMap.put("type", "start");
        eventMap.put("action", "http-request");
        eventMap.put("outcome", "unknown");

        return Map.of("http", httpMap, "url", urlMap, "event", eventMap);
    }

    private Optional<Integer> resolvePort(HttpRequest httpRequest) {
        String port = structuredHttpLogFormatter.preparePort(httpRequest);
        try {
            if (port != null) {
                return Optional.of(Integer.valueOf(port));
            }
        } catch (Exception e) {
            log.debug("Unable to resolve port", e);
        }

        return Optional.empty();
    }

    @Override
    public Map<String, Object> resolveMembers(Correlation correlation, HttpResponse httpResponse) throws IOException {
        Map<String, Object> responseMap = new LinkedHashMap<>();
        responseMap.put("id", correlation.getId());
        responseMap.put("status_code", httpResponse.getStatus());

        structuredHttpLogFormatter.prepareBody(httpResponse).map(Object::toString).ifPresent(body -> responseMap.put("body", Map.of("content", body, "bytes", body.getBytes(StandardCharsets.UTF_8).length)));

        Map<String, Object> httpMap = new LinkedHashMap<>();
        httpMap.put("version", resolveHttpVersion(httpResponse.getProtocolVersion()));
        httpMap.put("response", responseMap);

        Map<String, Object> eventMap = new LinkedHashMap<>();
        eventMap.put("kind", "event");
        eventMap.put("category", "web");
        eventMap.put("type", "end");
        eventMap.put("action", "http-response");
        eventMap.put("outcome", resolveOutcome(httpResponse.getStatus()));
        eventMap.put("duration", correlation.getDuration().toNanos());

        return Map.of("http", httpMap, "event", eventMap);
    }

    private String resolveOutcome(int status) {
        if (status >= 400) {
            return "failure";
        }

        return "success";
    }

    private String resolveHttpVersion(String protocolVersion) {
        try {
            return protocolVersion.split("/")[1];
        } catch (Exception e) {
            log.debug("Unable to extract HTTP version", e);
            return "1.1";
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public String format(Map<String, Object> members) {
        if (members.containsKey("url")) {
            return String.format("Request to %s", ((Map<String, Object>) members.get("url")).get("full"));
        }

        return "null"; // TODO
    }

}
