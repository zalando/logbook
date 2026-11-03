package org.zalando.logbook.jdkhttpclient;

import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.Origin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.zip.GZIPInputStream;

final class RemoteResponse implements org.zalando.logbook.HttpResponse {

    private final HttpResponse<?> response;
    private final BufferingBodyHandler<?> bodyHandler;
    private final boolean decompressResponse;
    private boolean withBody;

    RemoteResponse(final HttpResponse<?> response, final BufferingBodyHandler<?> bodyHandler,
            final boolean decompressResponse) {
        this.response = response;
        this.bodyHandler = bodyHandler;
        this.decompressResponse = decompressResponse;
    }

    @Override
    public Origin getOrigin() {
        return Origin.REMOTE;
    }

    @Override
    public String getProtocolVersion() {
        return response.version().toString();
    }

    @Override
    public int getStatus() {
        return response.statusCode();
    }

    @Override
    public String getReasonPhrase() {
        return "";
    }

    @Override
    public HttpHeaders getHeaders() {
        return HttpHeaders.of(response.headers().map());
    }

    @Override
    public byte[] getBody() throws IOException {
        if (!withBody) {
            return new byte[0];
        }

        final byte[] body = bodyHandler.getBody();
        return decompressResponse && isGzip() ? decompress(body) : body;
    }

    @Override
    public RemoteResponse withBody() {
        withBody = true;
        return this;
    }

    @Override
    public RemoteResponse withoutBody() {
        withBody = false;
        return this;
    }

    private boolean isGzip() {
        return response.headers().allValues("Content-Encoding").stream()
                .anyMatch(value -> "gzip".equalsIgnoreCase(value) || "x-gzip".equalsIgnoreCase(value));
    }

    private static byte[] decompress(final byte[] body) throws IOException {
        try (GZIPInputStream input = new GZIPInputStream(new ByteArrayInputStream(body))) {
            return input.readAllBytes();
        }
    }
}
