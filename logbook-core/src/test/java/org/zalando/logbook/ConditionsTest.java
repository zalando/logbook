package org.zalando.logbook;

import org.junit.Test;

import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zalando.logbook.Conditions.contentType;
import static org.zalando.logbook.Conditions.exclude;
import static org.zalando.logbook.Conditions.header;
import static org.zalando.logbook.Conditions.requestTo;

public final class ConditionsTest {

    private final RawHttpRequest request = MockRawHttpRequest.create()
            .withHeaders(MockHeaders.of("X-Secret", "true"))
            .withContentType("text/plain");

    @Test
    public void excludeShouldMatchIfNoneMatches() {
        final Predicate<BaseHttpRequest> unit = exclude(requestTo("/admin"), contentType("application/json"));

        assertThat(unit.test(request), is(true));
    }

    @Test
    public void excludeNotShouldMatchIfAnyMatches() {
        final Predicate<BaseHttpRequest> unit = exclude(requestTo("/admin"), contentType("text/plain"));

        assertThat(unit.test(request), is(false));
    }

    @Test
    public void excludeNotShouldMatchIfAllMatches() {
        final Predicate<BaseHttpRequest> unit = exclude(requestTo("/"), contentType("text/plain"));

        assertThat(unit.test(request), is(false));
    }

    @Test
    public void excludeShouldDefaultToAlwaysTrue() {
        final Predicate<RawHttpRequest> unit = exclude();

        assertThat(unit.test(null), is(true));
    }

    @Test
    public void requestToShouldMatchURI() {
        final Predicate<BaseHttpRequest> unit = requestTo("http://localhost/");

        assertThat(unit.test(request), is(true));
    }

    @Test
    public void requestToShouldNotMatchURIPattern() {
        final Predicate<BaseHttpRequest> unit = requestTo("http://192.168.0.1/*");

        assertThat(unit.test(request), is(false));
    }

    @Test
    public void requestToShouldIgnoreQueryParameters() {
        final Predicate<BaseHttpRequest> unit = requestTo("http://localhost/*");

        final MockRawHttpRequest request = MockRawHttpRequest.create()
                .withQuery("location=/bar");

        assertThat(unit.test(request), is(true));
    }

    @Test
    public void requestToShouldMatchPath() {
        final Predicate<BaseHttpRequest> unit = requestTo("/");

        assertThat(unit.test(request), is(true));
    }

    @Test
    public void contentTypeShouldMatch() {
        final Predicate<BaseHttpMessage> unit = contentType("text/plain");

        assertThat(unit.test(request), is(true));
    }

    @Test
    public void contentTypeShouldNotMatch() {
        final Predicate<BaseHttpMessage> unit = contentType("application/json");

        assertThat(unit.test(request), is(false));
    }

    @Test
    public void headerShouldMatchNameAndValue() {
        final Predicate<BaseHttpMessage> unit = header("X-Secret", "true");

        assertThat(unit.test(request), is(true));
    }

    @Test
    public void headerShouldNotMatchNameAndValue() {
        final Predicate<BaseHttpMessage> unit = header("X-Secret", "false");

        assertThat(unit.test(request), is(false));
    }

    @Test
    public void headerShouldMatchNameAndValuePredicate() {
        final Predicate<BaseHttpMessage> unit = header("X-Secret", asList("true", "1")::contains);

        assertThat(unit.test(request), is(true));
    }

    @Test
    public void headerShouldNotMatchNameAndValuePredicate() {
        final Predicate<BaseHttpMessage> unit = header("X-Secret", asList("yes", "1")::contains);

        assertThat(unit.test(request), is(false));
    }

    @Test
    public void headerShouldMatchPredicate() {
        final Predicate<BaseHttpMessage> unit = header((name, value) ->
                name.equalsIgnoreCase("X-Secret") && value.equalsIgnoreCase("true"));

        assertThat(unit.test(request), is(true));
    }

    @Test
    public void headerShouldNotMatchPredicate() {
        final Predicate<BaseHttpMessage> unit = header((name, value) ->
                name.equalsIgnoreCase("X-Secret") && value.equalsIgnoreCase("false"));

        assertThat(unit.test(request), is(false));
    }

}
