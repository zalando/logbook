package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class BaseHttpMessageTest {

    @Test
    void shouldUseCaseInsensitiveHeaders() {
        final Map<String, List<String>> headers = new BaseHttpMessage.HeadersBuilder()
                .put("X-Secret", "s3cr3t")
                .put("X-Secret", "knowledge")
                .put("Y-Secret", Arrays.asList("one", "two"))
                .build();

        assertThat(headers.get("x-secret"), hasItem("s3cr3t"));
        assertThat(headers.get("x-secret"), hasItem("knowledge"));
        assertThat(headers.get("Y-SECRET"), hasItem("one"));
        assertThat(headers.get("Y-SECRET"), hasItem("two"));
    }

    @Test
    void shouldBuildImmutableHeaders() {
        final Map<String, List<String>> headers = new BaseHttpMessage.HeadersBuilder()
                .put("a", "b")
                .put("a", "c")
                .put("d", Arrays.asList("e", "f"))
                .build();

        assertThrows(UnsupportedOperationException.class, () ->
                headers.put("x", Arrays.asList("y", "z")));

        final List<String> a = headers.get("a");
        assertNotNull(a);
        assertThat(a, hasItems("b", "c"));

        assertThrows(UnsupportedOperationException.class, () ->
                a.add("x"));
    }

    @Test
    void shouldRefuseUpdateHeadersAfterBuild() {
        final BaseHttpMessage.HeadersBuilder builder = new BaseHttpMessage.HeadersBuilder();
        builder.put("a", "b").build();

        assertThrows(UnsupportedOperationException.class, () ->
                // existing key
                builder.put("a", "b"));

        assertThrows(UnsupportedOperationException.class, () ->
                // new key
                builder.put("x", "y"));
    }

}
