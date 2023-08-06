package org.zalando.logbook.attributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.zalando.logbook.HttpRequest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;
import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
@Immutable
@Slf4j
@EqualsAndHashCode(callSuper = true)
public final class JwtAllMatchingClaimsExtractor extends JwtBaseExtractor {

    // RFC 7519 section-4.1.2: The "sub" (subject) claim identifies the principal that is the subject of the JWT.
    public static final String DEFAULT_SUBJECT_CLAIM = "sub";

    private static final Marker LOG_MARKER = MarkerFactory.getMarker("JwtAllMatchingClaimsExtractor");

    public JwtAllMatchingClaimsExtractor(
            final ObjectMapper objectMapper,
            final List<String> claimNames,
            final boolean isExceptionLogged
    ) {
        super(objectMapper, Collections.unmodifiableList(claimNames), isExceptionLogged);
    }

    @API(status = STABLE)
    public static final class Builder {

    }

    @SuppressWarnings("unused")
    @lombok.Builder(builderClassName = "Builder")
    @Nonnull
    private static JwtAllMatchingClaimsExtractor create(
            @Nullable final ObjectMapper objectMapper,
            @Nullable final List<String> claimNames,
            @Nullable final Boolean isExceptionLogged
    ) {
        return new JwtAllMatchingClaimsExtractor(
                Optional.ofNullable(objectMapper).orElse(new ObjectMapper()),
                Optional.ofNullable(claimNames).orElse(Collections.singletonList(DEFAULT_SUBJECT_CLAIM)),
                Optional.ofNullable(isExceptionLogged).orElse(false)
        );
    }

    @Override
    public void logException(final Exception exception) {
        if (isExceptionLogged)
            log.trace(
                    LOG_MARKER,
                    "Encountered error while extracting attributes: `{}`",
                    (Optional.ofNullable(exception.getCause()).orElse(exception)).getMessage()
            );
    }

    @Nonnull
    @Override
    protected Marker getLogMarker() {
        return LOG_MARKER;
    }

    @Nonnull
    @Override
    public HttpAttributes extract(final HttpRequest request) {
        try {
            final Map<String, String> attributeMap = extractClaims(request).entrySet().stream()
                    .filter(e -> (e.getKey() instanceof String) && claimNames.contains(e.getKey()))
                    .collect(toMap(entry -> (String) entry.getKey(), entry -> toStringValue(entry.getValue())));

            return new HttpAttributes(attributeMap);
        } catch (Exception e) {
            logException(e);
            return HttpAttributes.EMPTY;
        }
    }

}
