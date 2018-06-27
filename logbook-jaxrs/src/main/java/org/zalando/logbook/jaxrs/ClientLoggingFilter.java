package org.zalando.logbook.jaxrs;

import java.io.IOException;
import java.util.Optional;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.Provider;

import org.zalando.logbook.Correlator;
import org.zalando.logbook.Logbook;

import static java.util.Optional.empty;
import static org.zalando.logbook.jaxrs.Accessor.retrieveLocalRequest;
import static org.zalando.logbook.jaxrs.Accessor.storeRequest;

@ConstrainedTo(RuntimeType.CLIENT)
@Provider
public class ClientLoggingFilter implements ClientRequestFilter, ClientResponseFilter {

  private Logbook logbook;

  public ClientLoggingFilter(Logbook logbook) {
    this.logbook = logbook;
  }

  @Override
  public void filter(ClientRequestContext requestContext) {
    storeRequest(requestContext, new LocalRequest(requestContext));
  }

  /**
   * This method receives the outgoing Response (along with the original Request).
   *
   * @param requestContext - request context
   * @param responseContext - response context
   */
  @Override
  public void filter(
      ClientRequestContext requestContext, ClientResponseContext responseContext
  ) throws IOException {
    Optional<LocalRequest> localRequestOpt = retrieveLocalRequest(requestContext);
    Optional<Correlator> correlatorOpt = empty();

    if (localRequestOpt.isPresent()) {
      correlatorOpt = logbook.write(localRequestOpt.get());
    }

    if (correlatorOpt.isPresent()) {
      correlatorOpt.get().write(new RemoteResponse(responseContext));
    }
  }
}
