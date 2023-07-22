package org.zalando.logbook.attributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apiguardian.api.API;
import org.zalando.logbook.HttpRequest;

import javax.annotation.Nonnull;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
@AllArgsConstructor
public final class JwtClaimExtractor implements RequestAttributesExtractor {

    private static final String BEARER_JWT_PATTERN = "Bearer [a-z0-9-_]+\\.([a-z0-9-_]+)\\.[a-z0-9-_]+";
    private static final Pattern pattern = Pattern.compile(BEARER_JWT_PATTERN, Pattern.CASE_INSENSITIVE);

    @Nonnull
    private final List<String> claimNames;

    public JwtClaimExtractor() {
        // RFC 7519 section-4.1.2:
        // The "sub" (subject) claim identifies the principal that is the subject of the JWT.
        this(Collections.singletonList("sub"));
    }

    @Nonnull
    @Override
    public HttpAttributes extract(HttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");

        if (authHeader == null) return HttpAttributes.EMPTY;

        Matcher matcher = pattern.matcher(authHeader);
        if (!matcher.matches()) return HttpAttributes.EMPTY;

        try {
            String payload = new String(Base64.getUrlDecoder().decode(matcher.group(1)));
            ObjectMapper objectMapper = new ObjectMapper();
            HashMap<?, ?> claims = objectMapper.readValue(payload, HashMap.class);
            return claimNames.stream()
                    .map(claims::get)
                    .filter(value -> value instanceof String)
                    .findFirst()
                    .map(value -> HttpAttributes.of("subject", value))
                    .orElse(HttpAttributes.EMPTY);
        } catch (Exception e) {
            return HttpAttributes.EMPTY;
        }
    }

}
