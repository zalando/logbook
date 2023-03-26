package org.zalando.logbook.httpclient5;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.io.entity.BasicHttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

final class RemoteResponseTest {

    private final BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream("Hello, world!".getBytes(UTF_8)), -1, ContentType.TEXT_PLAIN, "gzip", true);
    private final BasicClassicHttpResponse delegate = new BasicClassicHttpResponse( 200, "Ok");
    private final RemoteResponse unit = new RemoteResponse(delegate);

    @BeforeEach
    void setUpResponseBody() {
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
        unit.withBody();

        assertThat(delegate.getEntity().getContentEncoding()).isEqualTo("gzip");
    }

    @Test
    void shouldNotSwallowDelegatesChunkedFlagWhenTransformingEntity() throws IOException {
        unit.withBody();

        assertThat(delegate.getEntity().isChunked()).isTrue();
    }

    @Test
    void shouldNotSwallowDelegatesContentTypeWhenTransformingEntity() throws IOException {
        unit.withBody();

        assertThat(delegate.getEntity().getContentType()).isEqualTo(ContentType.TEXT_PLAIN.toString());
    }

    @Test
    void shouldReadBodyIfPresent() throws IOException {
        assertThat(new String(unit.withBody().getBody(), UTF_8)).isEqualTo("Hello, world!");
        assertThat(new String(EntityUtils.toByteArray(delegate.getEntity()), UTF_8)).isEqualTo("Hello, world!");
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

    @Test
    void shouldResolveProtocolVersion() throws IOException {
        unit.withBody();

        assertThat(unit.getProtocolVersion()).isEqualTo(HttpVersion.HTTP_1_1.toString());
    }

    @Test
    void shouldPreserveCaseForReasonPhrase() {
        assertThat(unit.getReasonPhrase()).isEqualTo("Ok");
    }
}