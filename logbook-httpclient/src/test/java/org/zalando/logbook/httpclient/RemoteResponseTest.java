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
import static org.assertj.core.api.Assertions.assertThat;

final class RemoteResponseTest {

    private final BasicHttpEntity entity = new BasicHttpEntity();
    private final HttpResponse delegate = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "OK");
    private final RemoteResponse unit = new RemoteResponse(delegate, false);

    @BeforeEach
    void setUpResponseBody() {
        entity.setContent(new ByteArrayInputStream("Hello, world!".getBytes(UTF_8)));
        delegate.setEntity(entity);
    }

    @Test
    void shouldReturnContentTypesCharsetIfGiven() {
        delegate.addHeader("Content-Type", "text/plain;charset=ISO-8859-1");

        assertThat(unit.getCharset()).isEqualTo(ISO_8859_1);
    }

    @Test
    void shouldReturnDefaultCharsetIfNoneGiven() {
        assertThat(unit.getCharset()).isEqualTo(UTF_8);
    }

    @Test
    void shouldNotReadNullBodyIfNotPresent() throws IOException {
        delegate.setEntity(null);

        assertThat(new String(unit.withBody().getBody(), UTF_8)).isEmpty();
        assertThat(delegate.getEntity()).isNull();
    }

    @Test
    void shouldNotSwallowDelegatesContentEncodingWhenTransformingEntity() throws IOException {
        entity.setContentEncoding("gzip");

        unit.withBody();

        assertThat(delegate.getEntity().getContentEncoding().getValue()).isEqualTo("gzip");
    }

    @Test
    void shouldNotSwallowDelegatesChunkedFlagWhenTransformingEntity() throws IOException {
        entity.setChunked(true);

        unit.withBody();

        assertThat(delegate.getEntity().isChunked()).isTrue();
    }

    @Test
    void shouldNotSwallowDelegatesContentTypeWhenTransformingEntity() throws IOException {
        entity.setContentType("application/json");

        unit.withBody();

        assertThat(delegate.getEntity().getContentType().getValue()).isEqualTo("application/json");
    }

    @Test
    void shouldReadBodyIfPresent() throws IOException {
        assertThat(new String(unit.withBody().getBody(), UTF_8)).isEqualTo("Hello, world!");
        assertThat(new String(toByteArray(delegate.getEntity()), UTF_8)).isEqualTo("Hello, world!");
    }

    @Test
    void shouldReturnEmptyBodyUntilCaptured() throws IOException {
        assertThat(new String(unit.getBody(), UTF_8)).isEmpty();
        assertThat(new String(unit.withBody().getBody(), UTF_8)).isEqualTo("Hello, world!");
    }

    @Test
    void shouldBeSafeAgainstCallingWithBodyTwice() throws IOException {
        assertThat(new String(unit.withBody().withBody().getBody(), UTF_8)).isEqualTo("Hello, world!");
    }

}
