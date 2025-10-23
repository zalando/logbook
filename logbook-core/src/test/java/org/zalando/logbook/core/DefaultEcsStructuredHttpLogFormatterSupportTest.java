package org.zalando.logbook.core;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.test.MockHttpRequest;
import org.zalando.logbook.test.MockHttpResponse;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultEcsStructuredHttpLogFormatterSupportTest {

    private final DefaultEcsStructuredHttpLogFormatterSupport defaultEcsStructuredHttpLogFormatterSupport = new DefaultEcsStructuredHttpLogFormatterSupport();

    @Test
    void shouldResolveHttpRequestMembers() throws IOException {
        // given
        Precorrelation precorrelation = new DefaultLogbook.SimplePrecorrelation("c9408eaa-677d-11e5-9457-10ddb1ee7671", Clock.systemUTC());
        HttpRequest httpRequest = MockHttpRequest.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(Origin.REMOTE)
                .withPath("/test")
                .withQuery("limit=1")
                .withBodyAsString("Hello, world!");

        // when
        Map<String, Object> membersMap = defaultEcsStructuredHttpLogFormatterSupport.resolveMembers(precorrelation, httpRequest);

        // then
        assertThat(membersMap).isNotEmpty();
    }

    @Test
    void shouldResolveHttpResponseMembers() throws IOException {
        // given
        Correlation correlation = new DefaultLogbook.SimpleCorrelation("c9408eaa-677d-11e5-9457-10ddb1ee7671", Instant.now(), Instant.now().plusSeconds(5));
        HttpResponse httpResponse = MockHttpResponse.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(Origin.REMOTE)
                .withStatus(201)
                .withBodyAsString("{\"success\":true}");

        // when
        Map<String, Object> membersMap = defaultEcsStructuredHttpLogFormatterSupport.resolveMembers(correlation, httpResponse);

        // then
        assertThat(membersMap).isNotEmpty();
    }

}
