package org.zalando.logbook.core.attributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.attributes.AttributeExtractor;

import javax.annotation.Nonnull;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
@EqualsAndHashCode
public abstract class JwtBaseExtractor implements AttributeExtractor {

    private static final String BEARER_JWT_PATTERN = "Bearer [a-z0-9-_]+\\.([a-z0-9-_]+)\\.[a-z0-9-_]+";
    private static final Pattern PATTERN = Pattern.compile(BEARER_JWT_PATTERN, Pattern.CASE_INSENSITIVE);

    @Nonnull
    protected final ObjectMapper objectMapper;

    @Nonnull
    protected final List<String> claimNames;

    protected Map<?,?> extractClaims(final HttpRequest request) throws JsonProcessingException {
        HttpHeaders headers = request.getHeaders();

        if (claimNames.isEmpty() || headers == null) return Collections.emptyMap();

        String authHeader = headers.getFirst("Authorization");
        if (authHeader == null) return Collections.emptyMap();

        Matcher matcher = PATTERN.matcher(authHeader);
        if (!matcher.matches()) return Collections.emptyMap();

        String payload = new String(Base64.getUrlDecoder().decode(matcher.group(1)));
        return objectMapper.readValue(payload, HashMap.class);
    }

    @Nonnull
    protected String toStringValue(final Object value) {
        try {
            return (value instanceof String) ? (String) value : objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
