package org.zalando.logbook;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

final class BodyReplacementRawHttpResponse implements ForwardingBaseHttpResponse, RawHttpResponse {

    private final RawHttpResponse response;
    private final String replacement;

    public BodyReplacementRawHttpResponse(final RawHttpResponse response, final String replacement) {
        this.response = response;
        this.replacement = replacement;
    }

    @Override
    public BaseHttpResponse delegate() {
        return response;
    }

    @Override
    public HttpResponse withBody() throws IOException {
        response.withoutBody();
        return new BodyReplacementHttpResponse(response, replacement);
    }

    private static final class BodyReplacementHttpResponse implements ForwardingBaseHttpResponse, HttpResponse {

        private final RawHttpResponse response;
        private final String body;

        public BodyReplacementHttpResponse(final RawHttpResponse response, final String body) {
            this.response = response;
            this.body = body;
        }

        @Override
        public BaseHttpResponse delegate() {
            return response;
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
