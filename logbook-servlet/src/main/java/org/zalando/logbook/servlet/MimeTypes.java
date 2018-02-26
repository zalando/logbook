package org.zalando.logbook.servlet;

import lombok.SneakyThrows;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.util.Optional;

final class MimeTypes {

    MimeTypes() {
        // package private so we can trick code coverage
    }

    static Optional<MimeType> parse(final String mimeType) {
        try {
            return Optional.of(new MimeType(mimeType));
        } catch (final MimeTypeParseException e) {
            return Optional.empty();
        }
    }

}
