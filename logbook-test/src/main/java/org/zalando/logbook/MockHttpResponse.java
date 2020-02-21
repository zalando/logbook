package org.zalando.logbook;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.FieldDefaults;
import org.apiguardian.api.API;

import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_8;
import static lombok.AccessLevel.PRIVATE;
import static org.apiguardian.api.API.Status.MAINTAINED;
import static org.zalando.logbook.Origin.LOCAL;

@API(status = MAINTAINED)
@FieldDefaults(level = PRIVATE)
@Getter
@With
@NoArgsConstructor(staticName = "create")
@AllArgsConstructor(access = PRIVATE)
public final class MockHttpResponse implements HttpResponse {

    String protocolVersion = "HTTP/1.1";
    Origin origin = LOCAL;
    int status = 200;
    HttpHeaders headers = HttpHeaders.empty();
    String contentType = "text/plain";
    Charset charset = UTF_8;
    String bodyAsString = "";

    @Override
    public byte[] getBody() {
        return bodyAsString.getBytes(UTF_8);
    }

    @Override
    public HttpResponse withBody() {
        return this;
    }

    @Override
    public HttpResponse withoutBody() {
        bodyAsString = "";
        return this;
    }

}
