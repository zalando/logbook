package org.zalando.logbook.test;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.FieldDefaults;
import org.apiguardian.api.API;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Origin;
import org.zalando.logbook.attributes.HttpAttributes;

import java.nio.charset.Charset;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static lombok.AccessLevel.PRIVATE;
import static org.apiguardian.api.API.Status.MAINTAINED;
import static org.zalando.logbook.Origin.REMOTE;

@API(status = MAINTAINED)
@FieldDefaults(level = PRIVATE)
@Getter
@With
@NoArgsConstructor(staticName = "create")
@AllArgsConstructor
public final class MockHttpRequest implements HttpRequest {

    String protocolVersion = "HTTP/1.1";
    Origin origin = REMOTE;
    String remote = "127.0.0.1";
    String method = "GET";
    String scheme = "http";
    String host = "localhost";
    Optional<Integer> port = Optional.of(80);
    String path = "/";
    String query = "";
    HttpHeaders headers = HttpHeaders.empty();
    String contentType = "text/plain";
    Charset charset = UTF_8;
    String bodyAsString = "";
    HttpAttributes httpAttributes = HttpAttributes.EMPTY;

    @Override
    public byte[] getBody() {
        return bodyAsString.getBytes(UTF_8);
    }

    @Override
    public HttpRequest withBody() {
        return this;
    }

    @Override
    public HttpRequest withoutBody() {
        bodyAsString = "";
        return this;
    }

    @Override
    public HttpAttributes getAttributes() {
        return httpAttributes;
    }

}
