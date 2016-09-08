package org.zalando.logbook;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

final class BodyReplacementRawHttpRequest implements ForwardingRawHttpRequest {

    private final RawHttpRequest request;
    private final String replacement;

    public BodyReplacementRawHttpRequest(final RawHttpRequest request, final String replacement) {
        this.request = request;
        this.replacement = replacement;
    }

    @Override
    public RawHttpRequest delegate() {
        return request;
    }

    @Override
    public HttpRequest withBody() throws IOException {
        request.withoutBody();
        return new BodyReplacementHttpRequest(request, replacement);
    }

    private static final class BodyReplacementHttpRequest implements ForwardingBaseHttpRequest, HttpRequest {

        private final RawHttpRequest request;
        private final String body;

        public BodyReplacementHttpRequest(final RawHttpRequest request, final String body) {
            this.request = request;
            this.body = body;
        }

        @Override
        public BaseHttpRequest delegate() {
            return request;
        }

        @Override
        public byte[] getBody() throws IOException {
            return body.getBytes(UTF_8);
        }

        @Override
        public String getBodyAsString() throws IOException {
            return body;
        }

    }

}
