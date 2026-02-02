package org.zalando.logbook.ecs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.CollectionUtils;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.StructuredHttpLogFormatter;
import org.zalando.logbook.autoconfigure.LogbookProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class EcsStructuredHttpLogFormatter implements StructuredHttpLogFormatter {

    private static final Set<String> EXCLUDED_NORMALIZED_HEADER_NAMES = Set.of("referer", "user_agent");

    private final ObjectProvider<LogbookProperties> logbookPropertiesObjectProvider;

    @Override
    public Map<String, Object> prepare(Precorrelation precorrelation, HttpRequest httpRequest)
            throws IOException {
        Map<String, Object> content = new HashMap<>();

        content.put("event.kind", "event");
        content.put("event.category", "web");
        content.put("event.type", "start");
        content.put("event.action", "http-request");
        content.put("event.outcome", "unknown");

        if (httpRequest.getOrigin() == Origin.LOCAL) {
            content.put("network.direction", "egress");
        } else {
            content.put("network.direction", "ingress");
        }

        Optional.ofNullable(httpRequest.getHeaders().getFirst("User-Agent"))
                .ifPresent(userAgent -> content.put("user_agent.original", userAgent));

        content.put(
                "http.version",
                resolveHttpVersion(httpRequest.getProtocolVersion())
        );
        content.put("http.request.id", precorrelation.getId());
        content.put("http.request.method", httpRequest.getMethod());
        Optional.ofNullable(httpRequest.getHeaders().getFirst("Referer"))
                .ifPresent(referrer -> content.put("http.request.referrer", referrer));

        String customNamespacePrefix = logbookPropertiesObjectProvider.getObject().getEcs().getCustomNamespacePrefix();
        httpRequest.getHeaders().forEach((headerName, headerValues) ->
        {
            String normalizeHeaderName = normalizeHeaderName(headerName);
            if (!EXCLUDED_NORMALIZED_HEADER_NAMES.contains(normalizeHeaderName)) {
                normalizeHeaderValues(headerValues).ifPresent(normalizedHeaderValue ->
                        content.put(String.format("%s.http.request.headers.%s", customNamespacePrefix,
                                normalizeHeaderName), normalizedHeaderValue));
            }
        });
        content.put(
                "logbook.http.request.headers_raw",
                httpRequest.getHeaders().entrySet().stream()
                        .map(entry -> entry.getKey() + ":" + String.join(",", entry.getValue()))
                        .collect(Collectors.joining("\n"))
        );

        prepareBody(httpRequest)
                .map(Object::toString)
                .ifPresent(body -> {
                    content.put("http.request.body.content", body);
                    content.put(
                            "http.request.body.bytes",
                            body.getBytes(StandardCharsets.UTF_8).length
                    );
                });

        Optional.ofNullable(httpRequest.getRequestUri())
                .ifPresent(full -> content.put("url.full", full));
        Optional.ofNullable(httpRequest.getScheme())
                .ifPresent(scheme -> content.put("url.scheme", scheme));
        Optional.ofNullable(httpRequest.getHost())
                .ifPresent(domain -> content.put("url.domain", domain));
        Optional.ofNullable(httpRequest.getPath())
                .ifPresent(path -> content.put("url.path", path));
        Optional.ofNullable(httpRequest.getQuery())
                .filter(query -> !query.isEmpty())
                .ifPresent(query -> content.put("url.query", query));
        resolvePort(httpRequest)
                .ifPresent(port -> content.put("url.port", port));

        return content;
    }

    private Optional<Integer> resolvePort(HttpRequest httpRequest) {
        String port = preparePort(httpRequest);
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
    public Map<String, Object> prepare(Correlation correlation, HttpResponse httpResponse)
            throws IOException {
        Map<String, Object> content = new HashMap<>();

        content.put("event.kind", "event");
        content.put("event.category", "web");
        content.put("event.type", "end");
        content.put("event.action", "http-response");
        content.put("event.outcome", resolveOutcome(httpResponse.getStatus()));
        content.put("event.duration", correlation.getDuration().toNanos());

        if (httpResponse.getOrigin() == Origin.LOCAL) {
            content.put("network.direction", "egress");
        } else {
            content.put("network.direction", "ingress");
        }

        Optional.ofNullable(httpResponse.getHeaders().getFirst("User-Agent"))
                .ifPresent(userAgent -> content.put("user_agent.original", userAgent));

        content.put(
                "http.version",
                resolveHttpVersion(httpResponse.getProtocolVersion())
        );
        content.put("http.response.id", correlation.getId());
        content.put("http.response.status_code", httpResponse.getStatus());

        String customNamespacePrefix = logbookPropertiesObjectProvider.getObject().getEcs().getCustomNamespacePrefix();
        httpResponse.getHeaders().forEach((headerName, headerValues) -> {
            String normalizeHeaderName = normalizeHeaderName(headerName);
            if (!EXCLUDED_NORMALIZED_HEADER_NAMES.contains(normalizeHeaderName)) {
                normalizeHeaderValues(headerValues).ifPresent(normalizedHeaderValue ->
                        content.put(String.format("%s.http.response.headers.%s", customNamespacePrefix,
                                normalizeHeaderName(headerName)), normalizedHeaderValue));
            }
        });
        content.put(
                "logbook.http.response.headers_raw",
                httpResponse.getHeaders().entrySet().stream()
                        .map(entry -> entry.getKey() + ":" + String.join(",", entry.getValue()))
                        .collect(Collectors.joining("\n"))
        );

        prepareBody(httpResponse)
                .map(Object::toString)
                .ifPresent(body -> {
                    content.put("http.response.body.content", body);
                    content.put(
                            "http.response.body.bytes",
                            body.getBytes(StandardCharsets.UTF_8).length
                    );
                });

        return content;
    }

    private static String resolveOutcome(int status) {
        if (status >= 400) {
            return "failure";
        }

        return "success";
    }

    private static String resolveHttpVersion(String protocolVersion) {
        try {
            return protocolVersion.split("/")[1];
        } catch (Exception e) {
            log.debug("Unable to extract HTTP version", e);
            return "1.1";
        }
    }

    protected String normalizeHeaderName(String headerName) {
        return headerName.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }

    protected Optional<String> normalizeHeaderValues(List<String> values) {
        if (!CollectionUtils.isEmpty(values)) {
            String normalizedValue = values.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .collect(Collectors.joining(","));
            if (!normalizedValue.isEmpty()) {
                return Optional.of(normalizedValue);
            }
        }

        return Optional.empty();
    }

    @Override
    public String format(Map<String, Object> content) {
        if ("http-request".equals(content.get("event.action"))) {
            return String.format("%s - Request to %s", content.get("http.request.id"), content.get("url.full"));
        }

        return String.format("%s - Response status code: %s", content.get("http.response.id"), content.get("http.response.status_code"));
    }

}
