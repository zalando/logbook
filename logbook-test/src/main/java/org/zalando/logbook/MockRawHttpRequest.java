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
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static lombok.AccessLevel.PRIVATE;
import static org.zalando.logbook.Origin.REMOTE;

@FieldDefaults(level = PRIVATE)
@Getter
@Wither
@NoArgsConstructor(staticName = "create")
@AllArgsConstructor
public final class MockRawHttpRequest implements RawHttpRequest {

    String protocolVersion = "HTTP/1.1";
    Origin origin = REMOTE;
    String remote = "127.0.0.1";
    String method = "GET";
    String scheme = "http";
    String host = "localhost";
    Optional<Integer> port = Optional.of(80);
    String path = "/";
    String query = "";
    Map<String, List<String>> headers = emptyMap();
    String contentType = "text/plain";
    Charset charset = UTF_8;
    String bodyAsString = "";

    @Override
    public MockHttpRequest withBody() throws IOException {
        return MockHttpRequest.create()
                .withProtocolVersion(protocolVersion)
                .withOrigin(origin)
                .withRemote(remote)
                .withMethod(method)
                .withScheme(scheme)
                .withHost(host)
                .withPort(port)
                .withPath(path)
                .withQuery(query)
                .withHeaders(headers)
                .withContentType(contentType)
                .withCharset(charset)
                .withBodyAsString(bodyAsString);
    }

}
