package org.zalando.logbook.autoconfigure;

import jakarta.annotation.Nonnull;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.attributes.AttributeExtractor;
import org.zalando.logbook.attributes.HttpAttributes;

import java.util.Optional;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * Extracts the authenticated principal name from the Spring Security context
 * and adds it to the Logbook HTTP attributes.
 *
 * <p>When Spring Security is present and a user is authenticated, this extractor
 * will include the principal name (e.g. username) in every request log entry
 * under the key {@code "principal"}.</p>
 *
 * <p>If no authentication is present or the user is not authenticated,
 * an empty attribute set is returned silently.</p>
 */
@API(status = EXPERIMENTAL)
@Slf4j
@EqualsAndHashCode
public final class SpringSecurityAttributeExtractor implements AttributeExtractor {

    static final String DEFAULT_PRINCIPAL_KEY = "principal";

    @Nonnull
    @Override
    public HttpAttributes extract(final HttpRequest request) {
        try {
            final Authentication authentication = SecurityContextHolder
                    .getContext()
                    .getAuthentication();

            // No authentication in context — user is not logged in or security is not active
            if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
                return HttpAttributes.EMPTY;
            }

            final String principalName = authentication.getName();

            // getName() should never return null for a valid Authentication, but guard anyway
            if (principalName == null || principalName.isBlank()) {
                return HttpAttributes.EMPTY;
            }

            return HttpAttributes.of(DEFAULT_PRINCIPAL_KEY, principalName);

        } catch (final Exception e) {
            log.trace(
                    "Encountered error while extracting Spring Security principal: `{}`",
                    Optional.ofNullable(e.getCause()).orElse(e).getMessage()
            );
            return HttpAttributes.EMPTY;
        }
    }

    @Nonnull
    @Override
    public HttpAttributes extract(final HttpRequest request, final HttpResponse response) {
        // Principal is established at request time — delegate to request extraction
        return extract(request);
    }
}
