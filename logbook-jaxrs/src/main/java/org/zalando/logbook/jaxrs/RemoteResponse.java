package org.zalando.logbook.jaxrs;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.ws.rs.client.ClientResponseContext;

import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;
import org.zalando.logbook.RawHttpResponse;

import static org.zalando.logbook.jaxrs.Utils.HTTP_1_1;
import static org.zalando.logbook.jaxrs.Utils.getCharsetOrUtf8;
import static org.zalando.logbook.jaxrs.Utils.getContentTypeOrNull;

public class RemoteResponse implements HttpResponse, RawHttpResponse {

  private final ClientResponseContext responseContext;
  private final byte[] body;

  public RemoteResponse(
      ClientResponseContext responseContext
  ) {
    this.responseContext = responseContext;

    // copy the body to the local byte array
    this.body = Utils.toByteArray(responseContext.getEntityStream());

    // set the read input stream back in the request so that it can be deserialized by resources
    responseContext.setEntityStream(new ByteArrayInputStream(body));
  }

  @Override
  public HttpResponse withBody() {
    return this;
  }

  @Override
  public byte[] getBody() {
    return Arrays.copyOf(body, body.length);
  }

  @Override
  public int getStatus() {
    return responseContext.getStatus();
  }

  @Override
  public String getProtocolVersion() {
    return HTTP_1_1;
  }

  @Override
  public Origin getOrigin() {
    return Origin.REMOTE;
  }

  @Override
  public Map<String, List<String>> getHeaders() {
    return responseContext.getHeaders();
  }

  @Nullable
  @Override
  public String getContentType() {
    return getContentTypeOrNull(responseContext.getMediaType());
  }

  @Override
  public Charset getCharset() {
    return getCharsetOrUtf8(responseContext.getMediaType());
  }
}
