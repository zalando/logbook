package org.zalando.logbook;


import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class ContentTypeTest {

    @Test
    void unitTest() {
        assertThat(ContentType.parseMimeType(null)).isNull();
        assertThat(ContentType.parseMimeType("")).isNull();

        assertThat(ContentType.parseCharset(null)).isNull();
        assertThat(ContentType.parseCharset("application/json; charset=")).isNull();
        assertThat(ContentType.parseCharset("application/json; charset=unknown-charset")).isNull();
        assertThat(ContentType.parseCharset("charset=;")).isNull();
        assertThat(ContentType.parseCharset("charset=foo\"")).isNull();
        assertThat(ContentType.parseCharset("charset=\"bar")).isNull();
        assertThat(ContentType.parseCharset("application/json; charset=\"us-ascii\"")).isEqualTo(StandardCharsets.US_ASCII);
    }
}