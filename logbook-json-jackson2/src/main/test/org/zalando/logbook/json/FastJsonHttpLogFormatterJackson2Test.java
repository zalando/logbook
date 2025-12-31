package org.zalando.logbook.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.json.JsonHttpLogFormatterJackson2Test.SimplePrecorrelation;
import org.zalando.logbook.test.MockHttpRequest;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY;
import static java.time.Clock.systemUTC;
import static org.zalando.logbook.Origin.REMOTE;

public class FastJsonHttpLogFormatterJackson2Test {
    private final ObjectMapper objectMapper;
    private final FastJsonHttpLogFormatterJackson2 formatter;

    public FastJsonHttpLogFormatterJackson2Test() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(FAIL_ON_READING_DUP_TREE_KEY);
        formatter = new FastJsonHttpLogFormatterJackson2(objectMapper);
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

        Assertions.assertDoesNotThrow(() -> objectMapper.readTree(json));
    }
}
