package org.zalando.logbook.httpclient5;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.experimental.UtilityClass;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


@UtilityClass
final class HttpEntities {

    interface Copy extends HttpEntity {
        byte[] getBody();
    }

    Copy copy(final HttpEntity entity) throws IOException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        entity.writeTo(os);
        final byte[] body = os.toByteArray();
        ContentType contentType = ContentType.parse(entity.getContentType());
        boolean chunked = entity.isChunked();
        String contentEncoding = entity.getContentEncoding();

        final ByteArrayEntity copy = new ByteArrayEntity(body, contentType, contentEncoding, chunked);

        return new DefaultCopy(copy, body);
    }

    @RequiredArgsConstructor
    private static final class DefaultCopy implements Copy {

        @Delegate
        private final HttpEntity entity;

        @Getter
        private final byte[] body;

    }

}
