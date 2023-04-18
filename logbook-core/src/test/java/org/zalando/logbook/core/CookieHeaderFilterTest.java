package org.zalando.logbook.core;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.zalando.logbook.api.HeaderFilter;
import org.zalando.logbook.api.HttpHeaders;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.zalando.logbook.core.HeaderFilters.replaceCookies;

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

    @ParameterizedTest
    @CsvSource({
            "'',''",
            "sessionToken=,sessionToken=",
            "sessionToken=abc123,sessionToken=321cba",
            "theme=light; sessionToken=abc123,theme=light; sessionToken=321cba",
            "sessionToken=abc123; userId=me,sessionToken=321cba; userId=em",
            "theme=light; sessionToken=abc123; userId=me,theme=light; sessionToken=321cba; userId=em"
    })
    void replacesCookieHeader(final String input, final String expected) {
        final Function<String, String> replacer = StringUtils::reverse;
        final List<String> bannedCookieNames = Arrays.asList("sessionToken", "userId");
        final HeaderFilter unit = replaceCookies(bannedCookieNames::contains, replacer);

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
