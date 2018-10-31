package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zalando.logbook.Conditions.contentType;
import static org.zalando.logbook.Conditions.exclude;
import static org.zalando.logbook.Conditions.header;
import static org.zalando.logbook.Conditions.requestTo;
import static org.zalando.logbook.Conditions.withoutContentType;

public final class ConditionsTest {

    private final MockHttpRequest request = MockHttpRequest.create()
            .withHeaders(MockHeaders.of("X-Secret", "true"))
            .withContentType("text/plain");

    @Test
    void excludeShouldMatchIfNoneMatches() {
        final Predicate<HttpRequest> unit = exclude(requestTo("/admin"), contentType("application/json"));

        assertThat(unit.test(request), is(true));
    }

    @Test
    void excludeNotShouldMatchIfAnyMatches() {
        final Predicate<HttpRequest> unit = exclude(requestTo("/admin"), contentType("text/plain"));

        assertThat(unit.test(request), is(false));
    }

    @Test
    void excludeNotShouldMatchIfAllMatches() {
        final Predicate<HttpRequest> unit = exclude(requestTo("/"), contentType("text/plain"));

        assertThat(unit.test(request), is(false));
    }

    @Test
    void excludeShouldDefaultToAlwaysTrue() {
        final Predicate<HttpRequest> unit = exclude();

        assertThat(unit.test(null), is(true));
    }

    @Test
    void requestToShouldMatchURI() {
        final Predicate<HttpRequest> unit = requestTo("http://localhost/");

        assertThat(unit.test(request), is(true));
    }

    @Test
    void requestToShouldNotMatchURIPattern() {
        final Predicate<HttpRequest> unit = requestTo("http://192.168.0.1/*");

        assertThat(unit.test(request), is(false));
    }

    @Test
    void requestToShouldIgnoreQueryParameters() {
        final Predicate<HttpRequest> unit = requestTo("http://localhost/*");

        final MockHttpRequest request = MockHttpRequest.create()
                .withQuery("location=/bar");

        assertThat(unit.test(request), is(true));
    }

    @Test
    void requestToShouldMatchPath() {
        final Predicate<HttpRequest> unit = requestTo("/");

        assertThat(unit.test(request), is(true));
    }

    @Test
    void contentTypeShouldMatch() {
        final Predicate<HttpMessage> unit = contentType("text/plain");

        assertThat(unit.test(request), is(true));
    }

    @Test
    void contentTypeShouldNotMatch() {
        final Predicate<HttpMessage> unit = contentType("application/json");

        assertThat(unit.test(request), is(false));
    }

    @Test
    void withoutContentTypeShouldMatch() {
        final Predicate<HttpMessage> unit = withoutContentType();

        assertThat(unit.test(request.withContentType(null)), is(true));
    }

    @Test
    void withoutContentTypeShouldNotMatch() {
        final Predicate<HttpMessage> unit = withoutContentType();

        assertThat(unit.test(request), is(false));
    }

    @Test
    void headerShouldMatchNameAndValue() {
        final Predicate<HttpMessage> unit = header("X-Secret", "true");

        assertThat(unit.test(request), is(true));
    }

    @Test
    void headerShouldNotMatchNameAndValue() {
        final Predicate<HttpMessage> unit = header("X-Secret", "false");

        assertThat(unit.test(request), is(false));
    }

    @Test
    void headerShouldMatchNameAndValuePredicate() {
        final Predicate<HttpMessage> unit = header("X-Secret", asList("true", "1")::contains);

        assertThat(unit.test(request), is(true));
    }

    @Test
    void headerShouldNotMatchNameAndValuePredicate() {
        final Predicate<HttpMessage> unit = header("X-Secret", asList("yes", "1")::contains);

        assertThat(unit.test(request), is(false));
    }

    @Test
    void headerShouldMatchPredicate() {
        final Predicate<HttpMessage> unit = header((name, value) ->
                name.equalsIgnoreCase("X-Secret") && value.equalsIgnoreCase("true"));

        assertThat(unit.test(request), is(true));
    }

    @Test
    void headerShouldNotMatchPredicate() {
        final Predicate<HttpMessage> unit = header((name, value) ->
                name.equalsIgnoreCase("X-Secret") && value.equalsIgnoreCase("false"));

        assertThat(unit.test(request), is(false));
    }

    @Test
    void headerShouldNotMatchPredicateWhenHeaderIsAbsent() {
        final Predicate<HttpMessage> unit = header("X-Absent", v -> true);

        assertThat(unit.test(request), is(false));
    }
}
