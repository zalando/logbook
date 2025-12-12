package org.zalando.logbook.json;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.json.JsonHttpLogFormatterJackson2Test.SimplePrecorrelation;
import org.zalando.logbook.test.MockHttpRequest;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.UUID;

import static java.time.Clock.systemUTC;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.zalando.logbook.Origin.REMOTE;

public class FastJsonHttpLogFormatterTest {
    private final JsonMapper jsonMapper;
    private final FastJsonHttpLogFormatter formatter;

    public FastJsonHttpLogFormatterTest() {
        jsonMapper = new JsonMapper();
        formatter = new FastJsonHttpLogFormatter(jsonMapper);
    }

    @Test
    public void shouldNotContainDuplicatedKeys() throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(REMOTE)
                .withPath("/test")
                .withHeaders(HttpHeaders.empty().update("Accept", "application/json"))
                .withContentType("application/json")
                .withBodyAsString("{\"action\": \"test\"}");

        String json = formatter.format(new SimplePrecorrelation(UUID.randomUUID().toString(), systemUTC()), request);

        assertDoesNotThrow(() -> jsonMapper.readTree(json));
    }
}
