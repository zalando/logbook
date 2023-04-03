package org.zalando.logbook.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.MockHttpRequest;
import org.zalando.logbook.api.HttpHeaders;
import org.zalando.logbook.api.HttpRequest;
import org.zalando.logbook.json.JsonHttpLogFormatterTest.SimplePrecorrelation;

import java.io.IOException;
import java.util.UUID;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY;
import static java.time.Clock.systemUTC;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.zalando.logbook.api.Origin.REMOTE;

public class FastJsonHttpLogFormatterTest {
    private final ObjectMapper objectMapper;
    private final FastJsonHttpLogFormatter formatter;

    public FastJsonHttpLogFormatterTest() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(FAIL_ON_READING_DUP_TREE_KEY);
        formatter = new FastJsonHttpLogFormatter(objectMapper);
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

        assertDoesNotThrow(() -> objectMapper.readTree(json));
    }
}
