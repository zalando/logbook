package org.zalando.logbook.httpclient;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public final class RemoteResponseTest {

    private final BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
    private final HttpResponse delegate = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "OK");
    private final RemoteResponse unit = new RemoteResponse(delegate);

    @BeforeEach
    public void setUpResponseBody() {
        basicHttpEntity.setContent(new ByteArrayInputStream("fooBar".getBytes(StandardCharsets.UTF_8)));
        delegate.setEntity(basicHttpEntity);
    }

    @Test
    void shouldReturnContentTypesCharsetIfGiven() {
        delegate.addHeader("Content-Type", "text/plain;charset=ISO-8859-1");

        assertThat(unit.getCharset(), is(StandardCharsets.ISO_8859_1));
    }

    @Test
    void shouldReturnDefaultCharsetIfNoneGiven() {
        assertThat(unit.getCharset(), is(StandardCharsets.UTF_8));
    }

    @Test
    void shouldNotReadEmptyBodyIfNotPresent() throws IOException {
        delegate.setEntity(null);

        assertThat(new String(unit.withBody().getBody(), UTF_8), is(emptyString()));
        assertThat(delegate.getEntity(), is(nullValue()));
    }

    @Test
    void shouldNotSwallowDelegatesContentEncodingWhenTransformingEntity() throws IOException {
        basicHttpEntity.setContentEncoding("gzip");

        unit.withBody();

        assertThat(delegate.getEntity().getContentEncoding().getValue(), is("gzip"));
    }

    @Test
    void shouldNotSwallowDelegatesChunkedFlagWhenTransformingEntity() throws IOException {
        basicHttpEntity.setChunked(true);

        unit.withBody();

        assertThat(delegate.getEntity().isChunked(), is(true));
    }

    @Test
    void shouldNotSwallowDelegatesContentTypeWhenTransformingEntity() throws IOException {
        basicHttpEntity.setContentType("application/json");

        unit.withBody();

        assertThat(delegate.getEntity().getContentType().getValue(), is("application/json"));
    }

}
