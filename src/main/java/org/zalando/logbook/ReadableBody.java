package org.zalando.logbook;

import java.io.IOException;

interface ReadableBody {

    String getCharacterEncoding();

    byte[] getBodyAsByteArray() throws IOException;

    default String getBodyAsString() throws IOException {
        return new String(getBodyAsByteArray(), getCharacterEncoding());
    }

}
