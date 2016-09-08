package org.zalando.logbook;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.Wither;

import java.io.IOException;
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
@AllArgsConstructor
public final class MockRawHttpResponse implements RawHttpResponse {

    String protocolVersion = "HTTP/1.1";
    Origin origin = LOCAL;
    int status = 200;
    Map<String, List<String>> headers = emptyMap();
    String contentType = "text/plain";
    Charset charset = UTF_8;
    String bodyAsString = "";

    @Override
    public MockHttpResponse withBody() throws IOException {
        return MockHttpResponse.create()
                .withProtocolVersion(protocolVersion)
                .withOrigin(origin)
                .withStatus(status)
                .withHeaders(headers)
                .withContentType(contentType)
                .withCharset(charset)
                .withBodyAsString(bodyAsString);
    }

}
