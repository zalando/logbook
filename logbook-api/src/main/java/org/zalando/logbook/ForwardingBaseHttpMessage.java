package org.zalando.logbook;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public interface ForwardingBaseHttpMessage extends BaseHttpMessage {

    BaseHttpMessage delegate();

    @Override
    default String getProtocolVersion() {
        return delegate().getProtocolVersion();
    }

    @Override
    default Origin getOrigin() {
        return delegate().getOrigin();
    }

    @Override
    default Map<String, List<String>> getHeaders() {
        return delegate().getHeaders();
    }

    @Override
    default String getContentType() {
        return delegate().getContentType();
    }

    @Override
    default Charset getCharset() {
        return delegate().getCharset();
    }

}
