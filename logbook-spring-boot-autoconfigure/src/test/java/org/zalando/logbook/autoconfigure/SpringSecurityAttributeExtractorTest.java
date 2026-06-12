package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.attributes.HttpAttributes;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpringSecurityAttributeExtractorTest {

    private final SpringSecurityAttributeExtractor extractor =
            new SpringSecurityAttributeExtractor();

    private final HttpRequest request = mock(HttpRequest.class);
    private final HttpResponse response = mock(HttpResponse.class);

    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void resetSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsEmptyWhenNoAuthenticationInContext() {
        // no authentication set — SecurityContext is empty
        final HttpAttributes attributes = extractor.extract(request);

        assertThat(attributes).isEmpty();
    }

    @Test
    void returnsEmptyWhenAuthenticationIsNull() {
        // explicitly set null authentication
        SecurityContextHolder.getContext().setAuthentication(null);

        final HttpAttributes attributes = extractor.extract(request);

        assertThat(attributes).isEmpty();
    }

    @Test
    void returnsEmptyWhenUserIsNotAuthenticated() {
        final Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(auth);

        final HttpAttributes attributes = extractor.extract(request);

        assertThat(attributes).isEmpty();
    }

    @Test
    void returnsEmptyWhenUserIsAnonymous() {
        // AnonymousAuthenticationToken — isAuthenticated() returns true but we treat it as unauthenticated
        final Authentication anonymous = new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        SecurityContextHolder.getContext().setAuthentication(anonymous);

        final HttpAttributes attributes = extractor.extract(request);

        assertThat(attributes).isEmpty();
    }

    @Test
    void returnsPrincipalNameWhenUserIsAuthenticated() {
        // fully authenticated user
        final Authentication auth = UsernamePasswordAuthenticationToken
                .authenticated("vinayak", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        final HttpAttributes attributes = extractor.extract(request);

        assertThat(attributes)
                .containsEntry(SpringSecurityAttributeExtractor.DEFAULT_PRINCIPAL_KEY, "vinayak");
    }

    @Test
    void returnsEmptyWhenAuthenticatedButNameIsNull() {
        final Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn(null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        final HttpAttributes attributes = extractor.extract(request);

        assertThat(attributes).isEmpty();
    }

    @Test
    void returnsEmptyWhenAuthenticatedButNameIsBlank() {
        final Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("");
        SecurityContextHolder.getContext().setAuthentication(auth);

        final HttpAttributes attributes = extractor.extract(request);

        assertThat(attributes).isEmpty();
    }

    @Test
    void extractWithResponseDelegatesToRequestExtraction() {
        // response-level extraction should behave exactly like request-level
        final Authentication auth = UsernamePasswordAuthenticationToken
                .authenticated("vinayak", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        final HttpAttributes fromRequest = extractor.extract(request);
        final HttpAttributes fromResponse = extractor.extract(request, response);

        assertThat(fromResponse).isEqualTo(fromRequest);
    }

    @Test
    void returnsEmptyWhenExceptionIsThrownDuringExtraction() {
        // simulate a broken Authentication that throws on getName()
        final Authentication broken = mock(Authentication.class);
        when(broken.isAuthenticated()).thenReturn(true);
        when(broken.getName()).thenThrow(new RuntimeException("context unavailable"));
        SecurityContextHolder.getContext().setAuthentication(broken);

        final HttpAttributes attributes = extractor.extract(request);

        // must never propagate the exception — silent fallback to empty
        assertThat(attributes).isEmpty();
    }

    @Test
    void equalsAndHashCodeWorkCorrectly() {
        final SpringSecurityAttributeExtractor first = new SpringSecurityAttributeExtractor();
        final SpringSecurityAttributeExtractor second = new SpringSecurityAttributeExtractor();

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }
}
