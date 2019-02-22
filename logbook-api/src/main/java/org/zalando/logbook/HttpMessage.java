package org.zalando.logbook;

import org.apiguardian.api.API;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface HttpMessage {

    String getProtocolVersion();

    Origin getOrigin();

    Map<String, List<String>> getHeaders();

    @Nullable
    String getContentType();

    Charset getCharset();

    byte[] getBody() throws IOException;

    default String getBodyAsString() throws IOException {
        return new String(getBody(), getCharset());
    }

}
