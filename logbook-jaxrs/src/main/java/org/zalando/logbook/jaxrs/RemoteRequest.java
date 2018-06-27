package org.zalando.logbook.jaxrs;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;

import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Origin;
import org.zalando.logbook.RawHttpRequest;

import static java.util.Optional.ofNullable;
import static org.zalando.logbook.jaxrs.Utils.EMPTY_STRING;
import static org.zalando.logbook.jaxrs.Utils.HTTP_1_1;
import static org.zalando.logbook.jaxrs.Utils.getCharsetOrUtf8;
import static org.zalando.logbook.jaxrs.Utils.getContentTypeOrNull;
import static org.zalando.logbook.jaxrs.Utils.getPortOptional;

public class RemoteRequest implements HttpRequest, RawHttpRequest {

  private final ContainerRequestContext requestContext;
  private final URI uri;
  private final MultivaluedMap<String, String> headers;
  private final String method;
  private final byte[] body;

  public RemoteRequest(ContainerRequestContext requestContext) {
    this.requestContext = requestContext;
    this.uri = requestContext.getUriInfo().getRequestUri();
    this.headers = requestContext.getHeaders();
    this.method = requestContext.getMethod();

    // copy the body to the local byte array
    this.body = Utils.toByteArray(requestContext.getEntityStream());

    // set the read input stream back in the request so that it can be deserialized by resources
    requestContext.setEntityStream(new ByteArrayInputStream(body));
  }

  @Override
  public HttpRequest withBody() {
    return this;
  }

  @Override
  public String getRemote() {
    return uri.getAuthority();
  }

  @Override
  public String getMethod() {
    return method;
  }

  @Override
  public String getScheme() {
    return uri.getScheme();
  }

  @Override
  public String getHost() {
    return uri.getHost();
  }

  @Override
  public Optional<Integer> getPort() {
    return getPortOptional(uri);
  }

  @Override
  public String getPath() {
    return uri.getPath();
  }

  @Override
  public String getQuery() {
    return ofNullable(uri.getQuery())
        .orElse(EMPTY_STRING);
  }

  @Override
  public byte[] getBody() {
    return Arrays.copyOf(body, body.length);
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
    return requestContext.getHeaders();
  }

  @Nullable
  @Override
  public String getContentType() {
    return getContentTypeOrNull(headers);
  }

  @Override
  public Charset getCharset() {
    return getCharsetOrUtf8(headers);
  }
}
