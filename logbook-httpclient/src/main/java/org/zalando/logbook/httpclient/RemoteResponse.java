package org.zalando.logbook.httpclient;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.zalando.fauxpas.FauxPas.throwingUnaryOperator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.Origin;

@AllArgsConstructor
public class RemoteResponse implements org.zalando.logbook.HttpResponse {

    private final AtomicReference<State> state = new AtomicReference<>(new Unbuffered());
    private final HttpResponse response;

    private interface State {

        default State with() {
            return this;
        }

        default State without() {
            return this;
        }

        default State buffer(final HttpResponse response) throws IOException {
            return this;
        }

        default byte[] getBody() {
            return new byte[0];
        }

    }

    private static final class Unbuffered implements State {

        @Override
        public State with() {
            return new Offering();
        }

    }

    private static final class Offering implements State {

        @Override
        public State without() {
            return new Unbuffered();
        }

        @Override
        public State buffer(final HttpResponse response) throws IOException {
            @Nullable final HttpEntity entity = response.getEntity();

            if (entity == null) {
                return new Passing();
            } else {
                final HttpEntities.Copy copy = HttpEntities.copy(entity);
                response.setEntity(copy);
                return new Buffering(copy.getBody());
            }
        }

    }

    @AllArgsConstructor
    private static final class Buffering implements State {

        private final byte[] body;

        @Override
        public State without() {
            return new Ignoring(this);
        }

        @Override
        public byte[] getBody() {
            return body;
        }

    }

    @AllArgsConstructor
    private static final class Ignoring implements State {

        private final Buffering buffering;

        @Override
        public State with() {
            return buffering;
        }

    }

    private static final class Passing implements State {

    }

    @Override
    public Origin getOrigin() {
        return Origin.REMOTE;
    }

    @Override
    public String getProtocolVersion() {
        return response.getStatusLine().getProtocolVersion().toString();
    }

    @Override
    public int getStatus() {
        return response.getStatusLine().getStatusCode();
    }

    @Override
    public HttpHeaders getHeaders() {
        HttpHeaders headers = HttpHeaders.empty();

        final Set<Map.Entry<String, List<String>>> entries =
            Stream.of(response.getAllHeaders())
                  .collect(groupingBy(
                      Header::getName,
                      mapping(Header::getValue, toList())))
                  .entrySet();

        for (final Map.Entry<String, List<String>> entry : entries) {
            final String name = entry.getKey();
            final List<String> values = entry.getValue();

            headers = headers.update(name, values);
        }

        return headers;
    }

    @Override
    public String getContentType() {
        return Optional.of(response)
                       .map(response -> response.getFirstHeader("Content-Type"))
                       .map(Header::getValue)
                       .orElse("");
    }

    @Override
    public Charset getCharset() {
        return Optional.of(response)
                       .map(response -> response.getFirstHeader("Content-Type"))
                       .map(Header::getValue)
                       .map(ContentType::parse)
                       .map(ContentType::getCharset)
                       .orElse(UTF_8);
    }

    @Override
    public org.zalando.logbook.HttpResponse withBody() throws IOException {
        state.updateAndGet(State::with);
        return this;
    }

    @Override
    public RemoteResponse withoutBody() {
        state.updateAndGet(State::without);
        return this;
    }

    private static byte[] getDecompressedBytes(byte[] body) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(body); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = gzipInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        }
    }

    @Override
    public byte[] getBody() throws IOException {
        byte[] body = state.updateAndGet(throwingUnaryOperator(s -> s.buffer(response))).getBody();
        if (isGzip()) {
            return getDecompressedBytes(body);
        }
        return body;
    }

    private boolean isGzip() {
        if (response.containsHeader("Content-Encoding")) {
            Header[] headers = response.getHeaders("Content-Encoding");
            return Arrays.stream(headers).anyMatch(header -> "gzip".equals(header.getValue()));
        }
        return false;
    }

}