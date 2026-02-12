package org.zalando.logbook.jaxrs;

import jakarta.ws.rs.core.MediaType;

import jakarta.annotation.Nullable;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Optional;

import static jakarta.ws.rs.core.MediaType.CHARSET_PARAMETER;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;

final class HttpMessages {

    private HttpMessages() {
    }

    static Charset getCharset(@Nullable final MediaType mediaType) {
        return ofNullable(mediaType)
                .map(MediaType::getParameters)
                .map(parameters -> parameters.get(CHARSET_PARAMETER))
                .map(Charset::forName)
                .orElse(UTF_8);
    }

    static Optional<Integer> getPort(final URI uri) {
        final int port = uri.getPort();
        return port == -1 ? Optional.empty() : Optional.of(port);
    }

}
