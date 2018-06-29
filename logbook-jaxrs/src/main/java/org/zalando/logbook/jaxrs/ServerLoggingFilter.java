package org.zalando.logbook.jaxrs;

import java.io.IOException;
import java.util.Optional;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.zalando.logbook.Correlator;
import org.zalando.logbook.Logbook;

import static org.zalando.logbook.jaxrs.Accessor.retrieveCorrelator;
import static org.zalando.logbook.jaxrs.Accessor.storeCorrelator;

@ConstrainedTo(RuntimeType.SERVER)
@Provider
public class ServerLoggingFilter implements ContainerRequestFilter,
    ContainerResponseFilter {
  private Logbook logbook;

  public ServerLoggingFilter(Logbook logbook) {
    this.logbook = logbook;
  }

  /**
   * This method receives the incoming Request.
   *
   * @param requestContext - request context
   */
  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
      storeCorrelator(
          requestContext,
          logbook.write(new RemoteRequest(requestContext))
      );
  }

  /**
   * This method receives the outgoing Response (along with the original Request).
   *
   * @param requestContext - request context
   * @param responseContext - response context
   */
  @Override
  public void filter(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext
  ) throws IOException {
    Optional<Correlator> correlatorOpt = retrieveCorrelator(requestContext);

    if(correlatorOpt.isPresent()){
        correlatorOpt.get().write(new LocalResponse(responseContext));
    }
  }
}
