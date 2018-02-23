package org.zalando.logbook.servlet;

import lombok.SneakyThrows;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

final class MimeTypes {

    MimeTypes() {
        // package private so we can trick code coverage
    }

    @SneakyThrows(MimeTypeParseException.class)
    static MimeType parse(final String mimeType) {
        return new MimeType(mimeType);
    }

}
