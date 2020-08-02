package org.zalando.logbook.httpclient;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.experimental.UtilityClass;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;

import java.io.IOException;

import static org.apache.http.util.EntityUtils.toByteArray;

@UtilityClass
final class HttpEntities {

    interface Copy extends HttpEntity {
        byte[] getBody();
    }

    Copy copy(final HttpEntity entity) throws IOException {
        final byte[] body = toByteArray(entity);

        final ByteArrayEntity copy = new ByteArrayEntity(body);
        copy.setChunked(entity.isChunked());
        copy.setContentEncoding(entity.getContentEncoding());
        copy.setContentType(entity.getContentType());

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
