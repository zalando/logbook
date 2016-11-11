package org.zalando.logbook.httpclient;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public final class RemoteResponseTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private final BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
    private final HttpResponse delegate = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "OK");
    private final RemoteResponse unit = new RemoteResponse(delegate);

    @Before
    public void setUpResponseBody() {
        basicHttpEntity.setContent(new ByteArrayInputStream("fooBar".getBytes(StandardCharsets.UTF_8)));
        delegate.setEntity(basicHttpEntity);
    }

    @Test
    public void shouldReturnContentTypesCharsetIfGiven() {
        delegate.addHeader("Content-Type", "text/plain;charset=ISO-8859-1");
                
        assertThat(unit.getCharset(), is(StandardCharsets.ISO_8859_1));
    }

    @Test
    public void shouldReturnDefaultCharsetIfNoneGiven() {
        assertThat(unit.getCharset(), is(StandardCharsets.UTF_8));
    }
    
    @Test
    public void shouldNotReadEmptyBodyIfNotPresent() throws IOException {
        delegate.setEntity(null);

        assertThat(new String(unit.withBody().getBody(), UTF_8), is(emptyString()));
        assertThat(delegate.getEntity(), is(nullValue()));
    }

    @Test
    public void shouldNotSwallowDelegatesContentEncodingWhenTransformingEntity() throws IOException {
        basicHttpEntity.setContentEncoding("gzip");

        unit.withBody();

        assertThat(delegate.getEntity().getContentEncoding().getValue(), is("gzip"));
    }

    @Test
    public void shouldNotSwallowDelegatesChunkedFlagWhenTransformingEntity() throws IOException {
        basicHttpEntity.setChunked(true);

        unit.withBody();

        assertThat(delegate.getEntity().isChunked(), is(true));
    }

    @Test
    public void shouldNotSwallowDelegatesContentTypeWhenTransformingEntity() throws IOException {
        basicHttpEntity.setContentType("application/json");

        unit.withBody();

        assertThat(delegate.getEntity().getContentType().getValue(), is("application/json"));
    }

}