package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.zalando.logbook.HeaderFilters.replaceCookies;

final class CookieHeaderFilterTest {

    @Test
    void parsesSetCookieHeader() {
        final HeaderFilter unit = replaceCookies("sessionToken"::equals, "XXX");

        final HttpHeaders before = HttpHeaders.of(
                "Set-Cookie",
                "theme=light",
                "sessionToken=abc123; Path=/; Expires=Wed, 09 Jun 2021 10:18:14 GMT");

        final HttpHeaders after = unit.filter(before);

        assertThat(after)
                .containsEntry("Set-Cookie", asList(
                    "theme=light",
                    "sessionToken=XXX; Path=/; Expires=Wed, 09 Jun 2021 10:18:14 GMT"));
    }

    @Test
    void ignoresEmptySetCookieHeader() {
        final HeaderFilter unit = replaceCookies("sessionToken"::equals, "XXX");

        final HttpHeaders before = HttpHeaders.of("Set-Cookie", "");
        final HttpHeaders after = unit.filter(before);

        assertThat(after).containsEntry("Set-Cookie", singletonList(""));
    }

    @ParameterizedTest
    @CsvSource({
            "'',''",
            "sessionToken=,sessionToken=XXX",
            "sessionToken=abc123,sessionToken=XXX",
            "theme=light; sessionToken=abc123,theme=light; sessionToken=XXX",
            "sessionToken=abc123; userId=me,sessionToken=XXX; userId=me",
            "theme=light; sessionToken=abc123; userId=me,theme=light; sessionToken=XXX; userId=me"
    })
    void parsesCookieHeader(final String input, final String expected) {
        final HeaderFilter unit = replaceCookies("sessionToken"::equals, "XXX");

        final HttpHeaders before = HttpHeaders.of("Cookie", input);
        final HttpHeaders after = unit.filter(before);

        assertThat(after).containsEntry("Cookie", singletonList(expected));
    }

    @Test
    void ignoresNoCookieHeaders() {
        final HeaderFilter unit = replaceCookies("sessionToken"::equals, "XXX");
        final HttpHeaders after = unit.filter(HttpHeaders.empty());
        assertThat(after).isEmpty();
    }

}
