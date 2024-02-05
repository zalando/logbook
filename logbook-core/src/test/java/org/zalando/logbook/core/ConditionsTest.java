package org.zalando.logbook.core;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpMessage;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.test.MockHttpRequest;

import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.zalando.logbook.core.Conditions.*;

final class ConditionsTest {

    private final MockHttpRequest request = MockHttpRequest.create()
            .withHeaders(HttpHeaders.of("X-Secret", "true"))
            .withContentType("text/plain");

    @Test
    void excludeShouldMatchIfNoneMatches() {
        final Predicate<HttpRequest> unit = exclude(requestTo("/admin"), contentType("application/json"));

        assertThat(unit.test(request)).isTrue();
    }

    @Test
    void excludeNotShouldMatchIfAnyMatches() {
        final Predicate<HttpRequest> unit = exclude(requestTo("/admin"), contentType("text/plain"));

        assertThat(unit.test(request)).isFalse();
    }

    @Test
    void excludeNotShouldMatchIfAllMatches() {
        final Predicate<HttpRequest> unit = exclude(requestTo("/"), contentType("text/plain"));

        assertThat(unit.test(request)).isFalse();
    }

    @Test
    void excludeShouldDefaultToAlwaysTrue() {
        final Predicate<HttpRequest> unit = exclude();

        assertThat(unit.test(null)).isTrue();
    }

    @Test
    void requestToShouldMatchURI() {
        final Predicate<HttpRequest> unit = requestTo("http://localhost/");

        assertThat(unit.test(request)).isTrue();
    }

    @Test
    void requestToShouldNotMatchURIPattern() {
        final Predicate<HttpRequest> unit = requestTo("http://192.168.0.1/*");

        assertThat(unit.test(request)).isFalse();
    }

    @Test
    void requestToShouldIgnoreQueryParameters() {
        final Predicate<HttpRequest> unit = requestTo("http://localhost/*");

        final MockHttpRequest request = MockHttpRequest.create()
                .withQuery("location=/bar");

        assertThat(unit.test(request)).isTrue();
    }

    @Test
    void requestToShouldMatchPath() {
        final Predicate<HttpRequest> unit = requestTo("/");

        assertThat(unit.test(request)).isTrue();
    }

    @Test
    void contentTypeShouldMatch() {
        final Predicate<HttpMessage> unit = contentType("text/plain");

        assertThat(unit.test(request)).isTrue();
    }

    @Test
    void contentTypeShouldNotMatch() {
        final Predicate<HttpMessage> unit = contentType("application/json");

        assertThat(unit.test(request)).isFalse();
    }

    @Test
    void withoutContentTypeShouldMatch() {
        final Predicate<HttpMessage> unit = withoutContentType();

        assertThat(unit.test(request.withContentType(null))).isTrue();
    }

    @Test
    void withoutContentTypeShouldNotMatch() {
        final Predicate<HttpMessage> unit = withoutContentType();

        assertThat(unit.test(request)).isFalse();
    }

    @Test
    void headerShouldMatchNameAndValue() {
        final Predicate<HttpMessage> unit = header("X-Secret", "true");

        assertThat(unit.test(request)).isTrue();
    }

    @Test
    void headerShouldNotMatchNameAndValue() {
        final Predicate<HttpMessage> unit = header("X-Secret", "false");

        assertThat(unit.test(request)).isFalse();
    }

    @Test
    void headerShouldMatchNameAndValuePredicate() {
        final Predicate<HttpMessage> unit = header("X-Secret", asList("true", "1")::contains);

        assertThat(unit.test(request)).isTrue();
    }

    @Test
    void headerShouldNotMatchNameAndValuePredicate() {
        final Predicate<HttpMessage> unit = header("X-Secret", asList("yes", "1")::contains);

        assertThat(unit.test(request)).isFalse();
    }

    @Test
    void headerShouldMatchPredicate() {
        final Predicate<HttpMessage> unit = header((name, value) ->
                name.equalsIgnoreCase("X-Secret") && value.equalsIgnoreCase("true"));

        assertThat(unit.test(request)).isTrue();
    }

    @Test
    void headerShouldNotMatchPredicate() {
        final Predicate<HttpMessage> unit = header((name, value) ->
                name.equalsIgnoreCase("X-Secret") && value.equalsIgnoreCase("false"));

        assertThat(unit.test(request)).isFalse();
    }

    @Test
    void headerShouldNotMatchPredicateWhenHeaderIsAbsent() {
        final Predicate<HttpMessage> unit = header("X-Absent", v -> true);

        assertThat(unit.test(request)).isFalse();
    }

    @Test
    void matchesWithoutHeader() {
        final Predicate<HttpMessage> unit = withoutHeader("Authorization");

        assertThat(unit.test(request)).isTrue();
    }

    @Test
    void doesNotMatchWithHeader() {
        final MockHttpRequest request = this.request
                .withHeaders(HttpHeaders.of("Authorization", "Bearer Unw62Gp9okJFN1AAHm8xtR"));

        final Predicate<HttpMessage> unit = withoutHeader("Authorization");

        assertThat(unit.test(request)).isFalse();
    }

    @Test
    void doesNotMatchWithEmptyHeaderValue() {
        final MockHttpRequest request = this.request
                .withHeaders(HttpHeaders.of("Authorization", ""));

        final Predicate<HttpMessage> unit = withoutHeader("Authorization");

        assertThat(unit.test(request)).isFalse();
    }

    @Test
    void matchesConditionalHeader() {
        final MockHttpRequest request = this.request
                .withHeaders(HttpHeaders.of("Authorization", "authorization"));

        final Predicate<HttpRequest> unit = conditionalHeader("Authorization", "authorization");

        assertThat(unit.test(request)).isTrue();
    }

    @Test
    void matchesConditionalHeaderWithoutHeader() {
        final MockHttpRequest request = this.request;

        final Predicate<HttpRequest> unit = conditionalHeader("Authorization", null);

        assertThat(unit.test(request)).isTrue();
    }

    @Test
    void doesNotMatchConditionalHeader() {
        final MockHttpRequest request = this.request
                .withHeaders(HttpHeaders.of("Authorization", "auth"));

        final Predicate<HttpRequest> unit = conditionalHeader("Authorization", "authorization");

        assertThat(unit.test(request)).isFalse();
    }


    @Test
    void shouldMatchMethod() {
        final MockHttpRequest request = this.request.withMethod("POST");

        final Predicate<HttpRequest> unit = requestWithMethod("POST");

        assertThat(unit.test(request)).isTrue();
    }

    @Test
    void shouldNotMatchMethod() {
        final MockHttpRequest request = this.request.withMethod("POST");

        final Predicate<HttpRequest> unit = requestWithMethod("GET");

        assertThat(unit.test(request)).isFalse();
    }

}
