package org.zalando.logbook;


import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class CompactingXmlBodyFilterTest {

    private BodyFilter unit = BodyFilters.compactXml();

    /*language=XML*/
    private final String prettifiedXml = "" +
            "<?xml version=\"1.0\"?>" +
            "<root>\n\n" +
            "    <child>text</child>\n" +
            "</root>";

    /*language=XML*/
    private final String minimisedXml = "<root><child>text</child></root>";

    @Test
    void shouldIgnoreEmptyBody() {
        final String filtered = unit.filter("application/xml", "");
        assertThat(filtered, is(""));
    }

    @Test
    void shouldIgnoreInvalidContent() {
        final String invalidBody = "<?xml>\n<invalid>";
        assertThat(unit.filter("application/xml", invalidBody), is(invalidBody));
    }

    @Test
    void shouldIgnoreInvalidContentType() {
        final String filtered = unit.filter("text/plain", prettifiedXml);
        assertThat(filtered, is(prettifiedXml));
    }

    @Test
    void shouldTransformValidXmlRequestWithSimpleContentType() {
        final String filtered = unit.filter("application/xml", prettifiedXml);
        assertThat(filtered, is(minimisedXml));
    }

    @Test
    void shouldTransformValidXmlRequestWithCompatibleContentType() {
        final String filtered = unit.filter("application/custom+xml", prettifiedXml);
        assertThat(filtered, is(minimisedXml));
    }

}
