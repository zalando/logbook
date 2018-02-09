package org.zalando.logbook.feignlogger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.zalando.logbook.Origin;
import org.zalando.logbook.RawHttpResponse;

import com.google.common.net.MediaType;

import feign.Response;
import feign.Response.Body;
import feign.Util;

final class RemoteResponse implements RawHttpResponse, org.zalando.logbook.HttpResponse {

    private final Response response;
    private byte[] body;

    RemoteResponse(final Response response) {
        this.response = response;
    }

    @Override
    public Origin getOrigin() {
        return Origin.REMOTE;
    }

    @Override
    public String getProtocolVersion() {
        return "1.1";
    }

    @Override
    public int getStatus() {
        return response.status();
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        final HeadersBuilder builder = new HeadersBuilder();

        for (Entry<String, Collection<String>> entry : response.headers().entrySet()) {
            builder.put(entry.getKey(), entry.getValue());
        }

        return builder.build();
    }

    @Override
    public String getContentType() {
        return Optional.ofNullable(response.headers().get("Content-Type"))
                .map(m -> m.iterator())
                .filter(m -> m.hasNext())
                .map(Iterator::next)
                .orElse("");
    }

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(response.headers().get("Content-Type"))
                .map(m -> m.iterator())
                .filter(m -> m.hasNext())
                .map(Iterator::next)
                .map(MediaType::parse)
                .map(e -> e.charset())
                .filter(e -> e.isPresent())
                .map(e -> e.get())
                .orElse(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] getBody() {
        return body != null ? body : new byte[] {};
    }

    @Override
    public org.zalando.logbook.HttpResponse withBody() throws IOException {

        Body body = response.body();
        if (body != null) {
            this.body = Util.toByteArray(response.body().asInputStream());
            
            response.toBuilder().body(this.body).build();
        }          

        return this;
    }

}
