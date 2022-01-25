package org.zalando.logbook.openfeign;

import feign.Response;
import lombok.RequiredArgsConstructor;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
final class RemoteResponse implements HttpResponse {
    private final int status;
    private final HttpHeaders headers;
    private final byte[] body;
    private final Charset charset;
    private boolean withBody = false;

    private static final String CHARSET_FLAG = "charset";

    public static RemoteResponse create(Response response, byte[] body) {
        return new RemoteResponse(
                response.status(),
                HeaderUtils.toLogbookHeaders(response.headers()),
                body,
                getCharsetByFeignVersion(response)
        );
    }

    private static Charset getCharsetByFeignVersion(Response response) {
        Field charsetField = Arrays.stream(response.getClass().getDeclaredFields())
                .filter(fieldName -> fieldName.getName()
                        .equalsIgnoreCase(CHARSET_FLAG)).findFirst().orElse(null);
        if(isNull(charsetField)) {
            //openFeign 10
            return Charset.defaultCharset();
        } else {
            //openFeign 11
            try {
                return (Charset) charsetField.get(response);
            } catch (Exception e) {
                return Charset.defaultCharset();
            }
        }
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getProtocolVersion() {
        // feign doesn't support HTTP/2, their own toString looks like this:
        // builder.append(httpMethod).append(' ').append(url).append(" HTTP/1.1\n");
        return "HTTP/1.1";
    }

    @Override
    public Origin getOrigin() {
        return Origin.REMOTE;
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    @Nullable
    @Override
    public String getContentType() {
        return Optional.ofNullable(headers.get("Content-Type"))
                .flatMap(ct -> ct.stream().findFirst())
                .orElse(null);
    }

    @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public HttpResponse withBody() {
        withBody = true;
        return this;
    }

    @Override
    public RemoteResponse withoutBody() {
        withBody = false;
        return this;
    }

    @Override
    public byte[] getBody() {
        return withBody && body != null ? body : new byte[0];
    }
}
