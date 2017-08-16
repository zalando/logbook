package org.zalando.logbook;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.Wither;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static lombok.AccessLevel.PRIVATE;
import static org.zalando.logbook.Origin.LOCAL;

@FieldDefaults(level = PRIVATE)
@Getter
@Wither
@NoArgsConstructor(staticName = "create")
@AllArgsConstructor(access = PRIVATE)
public final class MockHttpResponse implements HttpResponse {

    String protocolVersion = "HTTP/1.1";
    Origin origin = LOCAL;
    int status = 200;
    Map<String, List<String>> headers = emptyMap();
    String contentType = "text/plain";
    Charset charset = UTF_8;
    String bodyAsString = "";

    @Override
    public byte[] getBody() {
        return bodyAsString.getBytes(UTF_8);
    }

}
