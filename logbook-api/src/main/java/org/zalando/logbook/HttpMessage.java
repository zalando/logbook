package org.zalando.logbook;

import java.io.IOException;

public interface HttpMessage extends BaseHttpMessage {

    byte[] getBody() throws IOException;

    default String getBodyAsString() throws IOException {
        return new String(getBody(), getCharset());
    }

}
