package org.zalando.logbook.jaxrs;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.ws.rs.container.ContainerResponseContext;

import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;
import org.zalando.logbook.RawHttpResponse;

import static org.zalando.logbook.jaxrs.Utils.HTTP_1_1;
import static org.zalando.logbook.jaxrs.Utils.getCharsetOrUtf8;
import static org.zalando.logbook.jaxrs.Utils.getContentTypeOrNull;

public class LocalResponse implements HttpResponse, RawHttpResponse {


  private final ContainerResponseContext responseContext;
  private final TeeOutputStream teeOutputStream;

  public LocalResponse(
      ContainerResponseContext responseContext
  ) {
    this.responseContext = responseContext;
    this.teeOutputStream = new TeeOutputStream(responseContext.getEntityStream());
    responseContext.setEntityStream(teeOutputStream);
  }

  @Override
  public HttpResponse withBody() {
    return this;
  }

  @Override
  public byte[] getBody() {
    byte[] body = teeOutputStream.toByteArray();
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
    return Origin.LOCAL;
  }

  @Override
  public Map<String, List<String>> getHeaders() {
    return responseContext.getStringHeaders();
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
