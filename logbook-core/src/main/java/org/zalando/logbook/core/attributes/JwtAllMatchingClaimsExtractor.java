package org.zalando.logbook.core.attributes;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.attributes.AttributeExtractor;
import org.zalando.logbook.attributes.HttpAttributes;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import tools.jackson.databind.json.JsonMapper;

import static java.util.stream.Collectors.toMap;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * Extracts all matching claims from the JWT bearer token in the request Authorization header.
 * Jackson 3.x (tools.jackson namespace) version.
 */
@API(status = EXPERIMENTAL)
@Slf4j
@EqualsAndHashCode
public final class JwtAllMatchingClaimsExtractor implements AttributeExtractor {

    // RFC 7519 section-4.1.2: The "sub" (subject) claim identifies the principal that is the subject of the JWT.
    private static final String DEFAULT_SUBJECT_CLAIM = "sub";

    private final List<String> claimNames;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    public JwtAllMatchingClaimsExtractor(
            final JsonMapper jsonMapper,
            final List<String> claimNames
    ) {
        this.claimNames = claimNames;
        jwtClaimsExtractor = new JwtClaimsExtractor(jsonMapper, new ArrayList<>(claimNames));
    }

    @API(status = EXPERIMENTAL)
    public static final class Builder {

    }

    @SuppressWarnings("unused")
    @lombok.Builder(builderClassName = "Builder")
    @Nonnull
    private static JwtAllMatchingClaimsExtractor create(
            @Nullable final JsonMapper jsonMapper,
            @Nullable final List<String> claimNames
    ) {
        return new JwtAllMatchingClaimsExtractor(
                Optional.ofNullable(jsonMapper).orElse(new JsonMapper()),
                Optional.ofNullable(claimNames).orElse(Collections.singletonList(DEFAULT_SUBJECT_CLAIM))
        );
    }

    @Nonnull
    @Override
    public HttpAttributes extract(final HttpRequest request) {
        try {
            final Map<String, Object> attributeMap = jwtClaimsExtractor.extractClaims(request).entrySet().stream()
                    .filter(entry -> claimNames.contains(entry.getKey()))
                    .collect(toMap(Map.Entry::getKey, entry -> jwtClaimsExtractor.toStringValue(entry.getValue())));

            return new HttpAttributes(attributeMap);
        } catch (Exception e) {
            log.trace(
                    "Encountered error while extracting attributes: `{}`",
                    (Optional.ofNullable(e.getCause()).orElse(e)).getMessage()
            );
            return HttpAttributes.EMPTY;
        }
    }

}
