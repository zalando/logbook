package org.zalando.logbook;

import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public final class JsonHttpLogFormatterTest {

    private final HttpLogFormatter unit = new JsonHttpLogFormatter();

    @Test
    public void shouldLogRequest() throws IOException {
        final String json = unit.format("3ce91230-677b-11e5-87b7-10ddb1ee7671", new MockHttpRequest());

        with(json)
                .assertThat("$.remote", is("127.0.0.1"))
                .assertThat("$.method", is("POST"))
                .assertThat("$.uri", is("/test"))
                .assertThat("$.headers.*", hasSize(2))
                .assertThat("$.headers['Accept']", is(singletonList("application/json")))
                .assertThat("$.headers['Content-Type']", is(singletonList("text/plain")))
                .assertThat("$.params.*", hasSize(1))
                .assertThat("$.params['limit']", is(singletonList("1")))
                .assertThat("$.body", is("Hello, world!"));
    }

    @Test
    public void shouldLogResponse() throws IOException {
        final String json = unit.format("53de2640-677d-11e5-bc84-10ddb1ee7671", new MockHttpResponse());

        with(json)
                .assertThat("$.status", is(200))
                .assertThat("$.headers.*", hasSize(1))
                .assertThat("$.headers['Content-Type']", is(singletonList("application/json")))
                .assertThat("$.body", is("{\"success\":true}"));
    }

}