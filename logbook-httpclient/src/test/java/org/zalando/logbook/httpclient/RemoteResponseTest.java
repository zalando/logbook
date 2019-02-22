package org.zalando.logbook.httpclient;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.util.EntityUtils.toByteArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

final class RemoteResponseTest {

    private final BasicHttpEntity entity = new BasicHttpEntity();
    private final HttpResponse delegate = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "OK");
    private final RemoteResponse unit = new RemoteResponse(delegate);

    @BeforeEach
    void setUpResponseBody() {
        entity.setContent(new ByteArrayInputStream("Hello, world!".getBytes(UTF_8)));
        delegate.setEntity(entity);
    }

    @Test
    void shouldReturnContentTypesCharsetIfGiven() {
        delegate.addHeader("Content-Type", "text/plain;charset=ISO-8859-1");

        assertThat(unit.getCharset(), is(ISO_8859_1));
    }

    @Test
    void shouldReturnDefaultCharsetIfNoneGiven() {
        assertThat(unit.getCharset(), is(UTF_8));
    }

    @Test
    void shouldNotReadNullBodyIfNotPresent() throws IOException {
        delegate.setEntity(null);

        assertThat(new String(unit.withBody().getBody(), UTF_8), is(emptyString()));
        assertThat(delegate.getEntity(), is(nullValue()));
    }

    @Test
    void shouldNotSwallowDelegatesContentEncodingWhenTransformingEntity() throws IOException {
        entity.setContentEncoding("gzip");

        unit.withBody();

        assertThat(delegate.getEntity().getContentEncoding().getValue(), is("gzip"));
    }

    @Test
    void shouldNotSwallowDelegatesChunkedFlagWhenTransformingEntity() throws IOException {
        entity.setChunked(true);

        unit.withBody();

        assertThat(delegate.getEntity().isChunked(), is(true));
    }

    @Test
    void shouldNotSwallowDelegatesContentTypeWhenTransformingEntity() throws IOException {
        entity.setContentType("application/json");

        unit.withBody();

        assertThat(delegate.getEntity().getContentType().getValue(), is("application/json"));
    }

    @Test
    void shouldReadBodyIfPresent() throws IOException {
        assertThat(new String(unit.withBody().getBody(), UTF_8), is("Hello, world!"));
        assertThat(new String(toByteArray(delegate.getEntity()), UTF_8), is("Hello, world!"));
    }

    @Test
    void shouldReturnEmptyBodyUntilCaptured() throws IOException {
        assertThat(new String(unit.getBody(), UTF_8), is(emptyString()));
        assertThat(new String(unit.withBody().getBody(), UTF_8), is("Hello, world!"));
    }

    @Test
    void shouldBeSafeAgainstCallingWithBodyTwice() throws IOException {
        assertThat(new String(unit.withBody().withBody().getBody(), UTF_8), is("Hello, world!"));
    }

}
