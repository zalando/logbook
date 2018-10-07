package org.zalando.logbook;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class XmlCompactingBodyFilterTest {

    private XmlCompactingBodyFilter bodyFilter;

    /*language=XML*/
    private final String prettifiedXml = "" +
            "<?xml version=\"1.0\"?>" +
            "<root>\n\n" +
            "    <child>text</child>\n" +
            "</root>";

    /*language=XML*/
    private final String minimisedXml = "<root><child>text</child></root>";

    @BeforeEach
    void setUp() {
        bodyFilter = new XmlCompactingBodyFilter();
    }

    @Test
    void shouldIgnoreEmptyBody() {
        final String filtered = bodyFilter.filter("application/xml", "");
        assertThat(filtered, is(""));
    }

    @Test
    void shouldIgnoreInvalidContent() {
        final String invalidBody = UUID.randomUUID().toString();
        final String filtered = bodyFilter.filter("application/xml", invalidBody);
        assertThat(filtered, is(invalidBody));
    }

    @Test
    void shouldIgnoreInvalidContentType() {
        final String filtered = bodyFilter.filter("text/plain", prettifiedXml);
        assertThat(filtered, is(prettifiedXml));
    }

    @Test
    void shouldTransformValidXmlRequestWithSimpleContentType() {
        final String filtered = bodyFilter.filter("application/xml", prettifiedXml);
        assertThat(filtered, is(minimisedXml));
    }

    @Test
    void shouldTransformValidXmlRequestWithCompatibleContentType() {
        final String filtered = bodyFilter.filter("application/custom+xml", prettifiedXml);
        assertThat(filtered, is(minimisedXml));
    }

}