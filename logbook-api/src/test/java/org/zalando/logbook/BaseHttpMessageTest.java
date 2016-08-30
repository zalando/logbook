package org.zalando.logbook;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public final class BaseHttpMessageTest {

    @Test
    public void shouldUseCaseInsensitiveHeaders() {
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
    public void shouldBuildImmutableHeaders() {
        final Map<String, List<String>> headers = new BaseHttpMessage.HeadersBuilder()
            .put("a", "b")
            .put("a", "c")
            .put("d", Arrays.asList("e", "f"))
            .build();

        try {
            headers.put("x", Arrays.asList("y", "z"));
            fail("Headers supposed to be immutable");
        } catch (UnsupportedOperationException ex) {
        }

        final List<String> a = headers.get("a");
        assertNotNull(a);
        assertThat(a, hasItems("b", "c"));

        try {
            a.add("x");
            fail("Headers supposed to be immutable");
        } catch (UnsupportedOperationException ex) {
        }
    }

    @Test
    public void shouldRefuseUpdateHeadersAfterBuild() {
        final BaseHttpMessage.HeadersBuilder builder = new BaseHttpMessage.HeadersBuilder();
        builder.put("a", "b").build();

        try {
            // existing key
            builder.put("a", "b");
            fail("Builder can not be reused");
        } catch (UnsupportedOperationException ex) {
        }

        try {
            // new key
            builder.put("x", "y");
            fail("Builder can not be reused");
        } catch (UnsupportedOperationException ex) {
        }
    }
}
