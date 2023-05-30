package org.zalando.logbook;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;

public class ContentTypeTest {

    @ParameterizedTest
    @CsvSource(value = {
            "null, null",
            "'', null",
            "'charset=;', null",
            "'charset=foo\"', null",
            "'charset=\"bar', null",
            "'application/json; charset=\"us-ascii\"', 'US-ASCII'",
            "'application/problem+json; charset=\"us-ascii\"', 'US-ASCII'",
            "'Application/Problem+JSON; charset=\"UTF-16\"', 'UTF-16'",
            "'application/problem+json;charset=\"utf-16\"', 'UTF-16'",
    },
            nullValues = {"null"}
    )
    void unitTest(final String contentType, final Charset expectedCharset) {
        assertThat(ContentType.parseCharset(contentType)).isEqualTo(expectedCharset);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "null, null",
            "'', null",
            "'application/json; charset=\"us-ascii\"', 'application/json'",
            "'application/problem+json; charset=\"us-ascii\"', 'application/problem+json'",
            "'Application/Problem+JSON; charset=\"UTF-16\"', 'Application/Problem+JSON'",
            "'application/problem+json;charset=\"utf-16\"', 'application/problem+json'",
    },
            nullValues = {"null"}
    )
    void shouldParseMimeType(final String contentType, final String expectedCharset) {
        assertThat(ContentType.parseMimeType(contentType)).isEqualTo(expectedCharset);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "'application/json; charset=', 'UTF-8'",
            "'Application/JSON; charset=', 'UTF-8'",
            "'application/json; charset=unknown-charset', 'UTF-8'",
            "'application/problem+json; charset=unknown-charset', 'UTF-8'",
            "'application/problem+JSON; charset=unknown-charset', 'UTF-8'",
    }
    )
    void fallbackToUTF8WhenJson(final String contentType, final Charset expectedCharset) {
        assertThat(ContentType.parseCharset(contentType)).isEqualTo(expectedCharset);
    }
}
