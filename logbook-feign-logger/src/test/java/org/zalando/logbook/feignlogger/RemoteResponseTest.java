package org.zalando.logbook.feignlogger;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import feign.Response;

public final class RemoteResponseTest {

    private final Response baseResponse = Response
                .builder()
                .status(200)
                .reason("OK")
                .headers(Collections.<String, Collection<String>>emptyMap())
                .body("fooBar".getBytes(StandardCharsets.UTF_8))
                .build();

    @Test
    void shouldReturnContentTypesCharsetIfGiven() {
        Map<String, Collection<String>> headers = new HashMap<String, Collection<String>>();
        headers.put("Content-Type", Arrays.asList("text/plain;charset=ISO-8859-1"));

        RemoteResponse unit = new RemoteResponse(baseResponse.toBuilder().headers(headers).build());

        assertThat(unit.getCharset(), is(StandardCharsets.ISO_8859_1));
    }

    @Test
    void shouldReturnDefaultCharsetIfNoneGiven() {
        RemoteResponse unit = new RemoteResponse(baseResponse);
        assertThat(unit.getCharset(), is(StandardCharsets.UTF_8));
    }

    @Test
    void shouldNotReadEmptyBodyIfNotPresent() throws IOException {
        RemoteResponse unit = new RemoteResponse(baseResponse
                    .toBuilder()
                    .headers(Collections.<String, Collection<String>>emptyMap())
                    .body((byte[])null)
                    .build());

        assertThat(new String(unit.withBody().getBody(), UTF_8), is(emptyString()));
    }

}
