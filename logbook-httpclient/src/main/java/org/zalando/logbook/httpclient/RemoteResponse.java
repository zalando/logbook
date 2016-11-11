package org.zalando.logbook.httpclient;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.zalando.logbook.Origin;
import org.zalando.logbook.RawHttpResponse;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.util.EntityUtils.toByteArray;

final class RemoteResponse implements RawHttpResponse, org.zalando.logbook.HttpResponse {

    private final HttpResponse response;
    private byte[] body;

    RemoteResponse(final HttpResponse response) {
        this.response = response;
    }

    @Override
    public Origin getOrigin() {
        return Origin.REMOTE;
    }

    @Override
    public String getProtocolVersion() {
        return response.getStatusLine().getProtocolVersion().toString();
    }

    @Override
    public int getStatus() {
        return response.getStatusLine().getStatusCode();
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        final HeadersBuilder builder = new HeadersBuilder();

        for (final Header header : response.getAllHeaders()) {
            builder.put(header.getName(), header.getValue());
        }

        return builder.build();
    }

    @Override
    public String getContentType() {
        return Optional.of(response)
                .map(request -> request.getFirstHeader("Content-Type"))
                .map(Header::getValue)
                .orElse("");
    }

    @Override
    public Charset getCharset() {
        return Optional.of(response)
                .map(request -> request.getFirstHeader("Content-Type"))
                .map(Header::getValue)
                .map(ContentType::parse)
                .map(ContentType::getCharset)
                .orElse(UTF_8);
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    @Override
    public org.zalando.logbook.HttpResponse withBody() throws IOException {
        @Nullable final HttpEntity originalEntity = response.getEntity();

        if (originalEntity == null) {
            this.body = new byte[0];
            return this;
        }

        this.body = toByteArray(originalEntity);

        ByteArrayEntity byteArrayEntity = new ByteArrayEntity(body);
        byteArrayEntity.setChunked(originalEntity.isChunked());
        byteArrayEntity.setContentEncoding(originalEntity.getContentEncoding());
        byteArrayEntity.setContentType(originalEntity.getContentType());

        response.setEntity(byteArrayEntity);

        return this;
    }

}
