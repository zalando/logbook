package org.zalando.logbook.attributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apiguardian.api.API.Status.STABLE;

/**
 * Extracts a single claim from the JWT bearer token in the request Authorization header.
 * By default, the subject claim "sub" is extracted, but you can pass an (ordered) list of <code>claimNames</code>
 * to be scanned. The first claim in <code>claimNames</code> is then returned, or an empty attribute if no matching
 * claim is found.
 */
@API(status = STABLE)
@Slf4j
@AllArgsConstructor
public final class JwtFirstMatchingClaimExtractor implements AttributeExtractor {

    private static final String BEARER_JWT_PATTERN = "Bearer [a-z0-9-_]+\\.([a-z0-9-_]+)\\.[a-z0-9-_]+";
    private static final Pattern PATTERN = Pattern.compile(BEARER_JWT_PATTERN, Pattern.CASE_INSENSITIVE);
    private static final Marker LOG_MARKER = MarkerFactory.getMarker("JwtFirstMatchingClaimExtractor");

    // RFC 7519 section-4.1.2: The "sub" (subject) claim identifies the principal that is the subject of the JWT.
    public static final String DEFAULT_SUBJECT_CLAIM = "sub";

    public static final String DEFAULT_CLAIM_KEY = "subject";

    @Nonnull
    private final ObjectMapper objectMapper;

    @Nonnull
    private final List<String> claimNames;

    @Nonnull
    private final String claimKey;

    @Nonnull
    private final Boolean shouldLogErrors;

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
            @Nullable final Boolean shouldLogErrors
    ) {
        return new JwtFirstMatchingClaimExtractor(
                Optional.ofNullable(objectMapper).orElse(new ObjectMapper()),
                Optional.ofNullable(claimNames).orElse(Collections.singletonList(DEFAULT_SUBJECT_CLAIM)),
                Optional.ofNullable(claimKey).orElse(DEFAULT_CLAIM_KEY),
                Optional.ofNullable(shouldLogErrors).orElse(false)
        );
    }

    @Override
    public void logException(final Exception exception) {
        if (shouldLogErrors)
            log.trace(
                    LOG_MARKER,
                    "Encountered error while extracting attributes: `{}`",
                    (Optional.ofNullable(exception.getCause()).orElse(exception)).getMessage()
            );
    }

    @Nonnull
    @Override
    public HttpAttributes extract(final HttpRequest request) {
        try {
            HttpHeaders headers = request.getHeaders();

            if (claimNames.isEmpty() || headers == null) return HttpAttributes.EMPTY;

            String authHeader = headers.getFirst("Authorization");
            if (authHeader == null) return HttpAttributes.EMPTY;

            Matcher matcher = PATTERN.matcher(authHeader);
            if (!matcher.matches()) return HttpAttributes.EMPTY;

            String payload = new String(Base64.getUrlDecoder().decode(matcher.group(1)));
            HashMap<?, ?> claims = objectMapper.readValue(payload, HashMap.class);
            return claimNames.stream()
                    .map(claims::get)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .map(this::toHttpAttribute)
                    .orElse(HttpAttributes.EMPTY);
        } catch (Exception e) {
            logException(e);
            return HttpAttributes.EMPTY;
        }
    }

    @Nonnull
    private HttpAttributes toHttpAttribute(final Object value) {
        try {
            final String valueAsString = (value instanceof String) ?
                    (String) value : objectMapper.writeValueAsString(value);
            return HttpAttributes.of(claimKey, valueAsString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
