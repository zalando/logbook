package org.zalando.logbook.servlet;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.util.Optional;

final class MimeTypes {

    private MimeTypes() {

    }

    static Optional<MimeType> parse(final String mimeType) {
        try {
            return Optional.of(new MimeType(mimeType));
        } catch (final MimeTypeParseException e) {
            return Optional.empty();
        }
    }

}
