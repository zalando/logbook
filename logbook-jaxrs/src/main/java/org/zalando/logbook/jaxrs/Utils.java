package org.zalando.logbook.jaxrs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.MediaType.CHARSET_PARAMETER;

final class Utils {
  static final String LOCALHOST = "localhost";
  static final String HTTP_1_1 = "HTTP/1.1";
  static final String EMPTY_STRING = "";
  private static final String CONTENT_TYPE_HEADER = "Content-Type";
  private static final String CONTENT_ENCODING_HEADER = "Content-Encoding";
  private static final int BUFFER_SIZE_IN_BYTES = 4096;
  private static final int END_OF_STREAM_MARK = -1;
  private Utils() {}

  static void copy(InputStream is, OutputStream os) {
    try {
      Objects.requireNonNull(is);
      Objects.requireNonNull(os);
      final byte[] buffer = new byte[BUFFER_SIZE_IN_BYTES];

      while (true) {
        final int r = is.read(buffer);
        if (r == END_OF_STREAM_MARK) {
          break;
        }
        os.write(buffer, 0, r);
      }
    } catch (IOException ex) {
      throw new RequestResponseLogProcessingException(ex);
    }
  }

  static byte[] toByteArray(InputStream is) {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    copy(is, os);
    return os.toByteArray();
  }

  static Charset getCharsetOrUtf8(MultivaluedMap<String, String> headers) {
    return ofNullable(headers.getFirst(CONTENT_ENCODING_HEADER))
        .map(Charset::forName)
        .orElse(UTF_8);
  }

  static Charset getCharsetOrUtf8(MediaType mediaType) {
    return ofNullable(mediaType)
        .map(mt -> mt.getParameters().getOrDefault(CHARSET_PARAMETER, UTF_8.name()))
        .map(Charset::forName)
        .orElse(UTF_8);
  }

  static String getContentTypeOrNull(MultivaluedMap<String, String> headers) {
    return ofNullable(headers.getFirst(CONTENT_TYPE_HEADER))
        .orElse(null);
  }

  static String getContentTypeOrNull(MediaType mediaType) {
    return ofNullable(mediaType)
        .map(MediaType::toString)
        .orElse(null);
  }

  static Optional<Integer> getPortOptional(URI uri) {
    int port = uri.getPort();
    if (0 < port) {
      return Optional.of(port);
    } else {
      return Optional.empty();
    }
  }
}
