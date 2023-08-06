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
import java.util.Objects;
import java.util.Optional;

import static org.apiguardian.api.API.Status.STABLE;

/**
 * Extracts a single claim from the JWT bearer token in the request Authorization header.
 * By default, the subject claim "sub" is extracted, but you can pass an (ordered) list of <code>claimNames</code>
 * to be scanned. The first claim in <code>claimNames</code> is then returned, or an empty attribute if no matching
 * claim is found.
 */
@API(status = STABLE)
@Immutable
@Slf4j
@EqualsAndHashCode(callSuper = true)
public final class JwtFirstMatchingClaimExtractor extends JwtBaseExtractor {

    // RFC 7519 section-4.1.2: The "sub" (subject) claim identifies the principal that is the subject of the JWT.
    public static final String DEFAULT_SUBJECT_CLAIM = "sub";

    private static final Marker LOG_MARKER = MarkerFactory.getMarker("JwtFirstMatchingClaimExtractor");

    public static final String DEFAULT_CLAIM_KEY = "subject";

    @Nonnull
    private final String claimKey;

    public JwtFirstMatchingClaimExtractor(
            @Nonnull final ObjectMapper objectMapper,
            @Nonnull final List<String> claimNames,
            @Nonnull final String claimKey,
            final boolean isExceptionLogged
    ) {
        super(objectMapper, Collections.unmodifiableList(claimNames), isExceptionLogged);
        this.claimKey = claimKey;
    }

    @API(status = STABLE)
    public static final class Builder {

    }

    @SuppressWarnings("unused")
    @lombok.Builder(builderClassName = "Builder")
    @Nonnull
    private static JwtFirstMatchingClaimExtractor create(
            @Nullable final ObjectMapper objectMapper,
            @Nullable final List<String> claimNames,
            @Nullable final String claimKey,
            @Nullable final Boolean isExceptionLogged
    ) {
        return new JwtFirstMatchingClaimExtractor(
                Optional.ofNullable(objectMapper).orElse(new ObjectMapper()),
                Optional.ofNullable(claimNames).orElse(Collections.singletonList(DEFAULT_SUBJECT_CLAIM)),
                Optional.ofNullable(claimKey).orElse(DEFAULT_CLAIM_KEY),
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
            return claimNames.stream()
                    .map(extractClaims(request)::get)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .map(value -> HttpAttributes.of(claimKey, toStringValue(value)))
                    .orElse(HttpAttributes.EMPTY);
        } catch (Exception e) {
            logException(e);
            return HttpAttributes.EMPTY;
        }
    }

}
