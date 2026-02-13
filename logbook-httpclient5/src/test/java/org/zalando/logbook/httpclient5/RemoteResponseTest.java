package org.zalando.logbook.httpclient5;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.impl.BasicEntityDetails;
import org.apache.hc.core5.http.io.entity.BasicHttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

final class RemoteResponseTest {

    private final BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream("Hello, world!".getBytes(UTF_8)), -1, ContentType.TEXT_PLAIN, "gzip", true);
    private final BasicClassicHttpResponse delegate = new BasicClassicHttpResponse(200, "Ok");
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
    void shouldReadBodyIfPresentAndEntityDetailsShowNegativeContentLength() throws IOException {
        final RemoteResponse underTest = new RemoteResponse(delegate, new BasicEntityDetails(-1, ContentType.TEXT_PLAIN), ByteBuffer.wrap("Hello, world!".getBytes(UTF_8)));
        assertThat(new String(underTest.withBody().getBody(), UTF_8)).isEqualTo("Hello, world!");
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

    @Test
    void shouldDecompressCompressedGzipBodyBeforeReturn() throws IOException {
        String json = "{\"data\": \"data\"}";
        byte[] compressed = compress(json.getBytes(StandardCharsets.UTF_8));

        BasicHttpEntity basicEntity = new BasicHttpEntity(
                new ByteArrayInputStream(compressed),
                -1,
                ContentType.APPLICATION_JSON,
                "gzip",
                true
        );

        BasicClassicHttpResponse underTest = new BasicClassicHttpResponse(200, "Ok");
        RemoteResponse response = new RemoteResponse(underTest, true);
        underTest.setEntity(basicEntity);
        underTest.addHeader("Content-Type", "application/json;charset=utf-8");
        underTest.addHeader("Content-Encoding", "gzip");

        assertThat(new String(response.withBody().getBody())).isEqualTo(json);
    }

    @Test
    void shouldDecompressCompressedXGzipBodyBeforeReturn() throws IOException {
        String json = "{\"data\": \"data\"}";
        byte[] compressed = compress(json.getBytes(StandardCharsets.UTF_8));

        BasicHttpEntity basicEntity = new BasicHttpEntity(
                new ByteArrayInputStream(compressed),
                -1,
                ContentType.APPLICATION_JSON,
                "gzip",
                true
        );

        BasicClassicHttpResponse underTest = new BasicClassicHttpResponse(200, "Ok");
        RemoteResponse response = new RemoteResponse(underTest, true);
        underTest.setEntity(basicEntity);
        underTest.addHeader("Content-Type", "application/json;charset=utf-8");
        underTest.addHeader("Content-Encoding", "x-gzip");

        assertThat(new String(response.withBody().getBody())).isEqualTo(json);
    }

    @Test
    void shouldNotDecompressCompressedBodyContentEncodingHeaderIsNotPresent() throws IOException {
        String json = "{\"data\": \"data\"}";
        byte[] compressed = compress(json.getBytes(StandardCharsets.UTF_8));

        BasicHttpEntity basicEntity = new BasicHttpEntity(
                new ByteArrayInputStream(compressed),
                -1,
                ContentType.APPLICATION_JSON,
                "gzip",
                true
        );

        BasicClassicHttpResponse underTest = new BasicClassicHttpResponse(200, "Ok");
        RemoteResponse response = new RemoteResponse(underTest, true);
        underTest.setEntity(basicEntity);
        underTest.addHeader("Content-Type", "application/json;charset=utf-8");

        assertThat(new String(response.withBody().getBody())).isNotEqualTo(json);
    }

    static byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(outputStream)) {
            gzipOut.write(data);
        }
        return outputStream.toByteArray();
    }
}
