package org.zalando.logbook.jaxrs;

import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.MediaType.CHARSET_PARAMETER;

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
