package org.zalando.logbook.core.attributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.attributes.AttributeExtractor;
import org.zalando.logbook.attributes.HttpAttributes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * Extracts a single claim from the JWT bearer token in the request Authorization header.
 * By default, the subject claim "sub" is extracted, but you can pass an (ordered) list of <code>claimNames</code>
 * to be scanned. The first claim in <code>claimNames</code> is then returned, or an empty attribute if no matching
 * claim is found.
 */
@API(status = EXPERIMENTAL)
@Slf4j
@EqualsAndHashCode
public final class JwtFirstMatchingClaimExtractor implements AttributeExtractor {

    // RFC 7519 section-4.1.2: The "sub" (subject) claim identifies the principal that is the subject of the JWT.
    private static final String DEFAULT_SUBJECT_CLAIM = "sub";
    private static final String DEFAULT_CLAIM_KEY = "subject";

    private final List<String> claimNames;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    @Nonnull
    private final String claimKey;

    public JwtFirstMatchingClaimExtractor(
            @Nonnull final ObjectMapper objectMapper,
            @Nonnull final List<String> claimNames,
            @Nonnull final String claimKey
    ) {
        this.claimNames = claimNames;
        this.claimKey = claimKey;
        jwtClaimsExtractor = new JwtClaimsExtractor(objectMapper, new ArrayList<>(claimNames));
    }

    @API(status = EXPERIMENTAL)
    public static final class Builder {

    }

    @SuppressWarnings("unused")
    @lombok.Builder(builderClassName = "Builder")
    @Nonnull
    private static JwtFirstMatchingClaimExtractor create(
            @Nullable final ObjectMapper objectMapper,
            @Nullable final List<String> claimNames,
            @Nullable final String claimKey
    ) {
        return new JwtFirstMatchingClaimExtractor(
                Optional.ofNullable(objectMapper).orElse(new ObjectMapper()),
                Optional.ofNullable(claimNames).orElse(Collections.singletonList(DEFAULT_SUBJECT_CLAIM)),
                Optional.ofNullable(claimKey).orElse(DEFAULT_CLAIM_KEY)
        );
    }

    @Nonnull
    @Override
    public HttpAttributes extract(final HttpRequest request) {
        try {
            return claimNames.stream()
                    .map(jwtClaimsExtractor.extractClaims(request)::get)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .map(value -> HttpAttributes.of(claimKey, jwtClaimsExtractor.toStringValue(value)))
                    .orElse(HttpAttributes.EMPTY);
        } catch (Exception e) {
            log.trace(
                    "Encountered error while extracting attributes: `{}`",
                    (Optional.ofNullable(e.getCause()).orElse(e)).getMessage()
            );
            return HttpAttributes.EMPTY;
        }
    }

}
