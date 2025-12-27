package org.zalando.logbook;

import org.apiguardian.api.API;

import jakarta.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface HttpMessage {

    default String getProtocolVersion() {
        return "HTTP/1.1";
    }

    Origin getOrigin();

    HttpHeaders getHeaders();

    @Nullable
    default String getContentType() {
        return Optional
                .ofNullable(getHeaders())
                .map(headers -> headers.getFirst(ContentType.CONTENT_TYPE_HEADER))
                .map(ContentType::parseMimeType)
                .orElse(null);
    }

    default Charset getCharset() {
        return Optional
                .ofNullable(getHeaders())
                .map(headers -> headers.getFirst(ContentType.CONTENT_TYPE_HEADER))
                .map(ContentType::parseCharset)
                .orElse(StandardCharsets.UTF_8);
    }

    byte[] getBody() throws IOException;

    default String getBodyAsString() throws IOException {
        return new String(getBody(), getCharset());
    }

}
