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
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
@Slf4j
@EqualsAndHashCode
public final class JwtAllMatchingClaimsExtractorJackson2 implements AttributeExtractor {

    // RFC 7519 section-4.1.2: The "sub" (subject) claim identifies the principal that is the subject of the JWT.
    private static final String DEFAULT_SUBJECT_CLAIM = "sub";

    private final List<String> claimNames;
    private final JwtClaimsExtractorJackson2 jwtClaimsExtractorJackson2;

    public JwtAllMatchingClaimsExtractorJackson2(
            final ObjectMapper objectMapper,
            final List<String> claimNames
    ) {
        this.claimNames = claimNames;
        jwtClaimsExtractorJackson2 = new JwtClaimsExtractorJackson2(objectMapper, new ArrayList<>(claimNames));
    }

    @API(status = EXPERIMENTAL)
    public static final class Builder {

    }

    @SuppressWarnings("unused")
    @lombok.Builder(builderClassName = "Builder")
    @Nonnull
    private static JwtAllMatchingClaimsExtractorJackson2 create(
            @Nullable final ObjectMapper objectMapper,
            @Nullable final List<String> claimNames
    ) {
        return new JwtAllMatchingClaimsExtractorJackson2(
                Optional.ofNullable(objectMapper).orElse(new ObjectMapper()),
                Optional.ofNullable(claimNames).orElse(Collections.singletonList(DEFAULT_SUBJECT_CLAIM))
        );
    }

    @Nonnull
    @Override
    public HttpAttributes extract(final HttpRequest request) {
        try {
            final Map<String, Object> attributeMap = jwtClaimsExtractorJackson2.extractClaims(request).entrySet().stream()
                    .filter(entry -> claimNames.contains(entry.getKey()))
                    .collect(toMap(Map.Entry::getKey, entry -> jwtClaimsExtractorJackson2.toStringValue(entry.getValue())));

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
