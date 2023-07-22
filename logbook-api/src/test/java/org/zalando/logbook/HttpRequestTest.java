package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.attributes.HttpAttributes;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

final class HttpRequestTest {

    @Test
    void httpRequestDoesNotSupportGetAttributesByDefault() {
        assertThat(new DefaultHttpRequest().getAttributes()).isEqualTo(HttpAttributes.EMPTY);
    }

    private static class DefaultHttpRequest implements HttpRequest {
        @Override
        public String getRemote() {
            return null;
        }

        @Override
        public String getMethod() {
            return null;
        }

        @Override
        public String getScheme() {
            return null;
        }

        @Override
        public String getHost() {
            return null;
        }

        @Override
        public Optional<Integer> getPort() {
            return Optional.empty();
        }

        @Override
        public String getPath() {
            return null;
        }

        @Override
        public String getQuery() {
            return null;
        }

        @Override
        public HttpRequest withBody() {
            return null;
        }

        @Override
        public HttpRequest withoutBody() {
            return null;
        }

        @Override
        public Origin getOrigin() {
            return null;
        }

        @Override
        public HttpHeaders getHeaders() {
            return null;
        }

        @Override
        public byte[] getBody() {
            return new byte[0];
        }
    }

}
