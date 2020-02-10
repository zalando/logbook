package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.zalando.logbook.HeaderFilters.replaceCookies;

final class CookieHeaderFilterTest {

    @Test
    void parsesSetCookieHeader() {
        final HeaderFilter unit = replaceCookies("sessionToken"::equals, "XXX");

        final Map<String, List<String>> before = singletonMap(
                "Set-Cookie", asList(
                        "theme=light",
                        "sessionToken=abc123; Path=/; Expires=Wed, 09 Jun 2021 10:18:14 GMT"));

        final Map<String, List<String>> after = unit.filter(before);

        assertThat(after, hasEntry("Set-Cookie", asList(
                "theme=light",
                "sessionToken=XXX; Path=/; Expires=Wed, 09 Jun 2021 10:18:14 GMT"
        )));
    }

    @Test
    void ignoresEmptySetCookieHeader() {
        final HeaderFilter unit = replaceCookies("sessionToken"::equals, "XXX");

        final Map<String, List<String>> before = singletonMap(
                "Set-Cookie", singletonList(""));

        final Map<String, List<String>> after = unit.filter(before);

        assertThat(after, hasEntry(
                "Set-Cookie", singletonList("")));
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

        final Map<String, List<String>> before = singletonMap(
                "Cookie", singletonList(input));

        final Map<String, List<String>> after = unit.filter(before);

        assertThat(after, hasEntry("Cookie", singletonList(expected)));
    }

    @Test
    void ignoresNoCookieHeaders() {
        final HeaderFilter unit = replaceCookies("sessionToken"::equals, "XXX");
        final Map<String, List<String>> after = unit.filter(emptyMap());
        assertThat(after, is(emptyMap()));
    }

}
