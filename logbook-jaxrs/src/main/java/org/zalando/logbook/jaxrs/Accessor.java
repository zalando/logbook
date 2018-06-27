package org.zalando.logbook.jaxrs;

import java.util.Optional;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.container.ContainerRequestContext;

import org.zalando.logbook.Correlator;

public final class Accessor {
  private static final String CONTAINER_PROPERTY_NAME = Accessor.class.getName() + ".CONTAINER_PROPERTY_NAME";
  private static final String CLIENT_PROPERTY_NAME = Accessor.class.getName() + ".CLIENT_PROPERTY_NAME";

  private Accessor(){}

  /**
   * Stores the Correlator in the request context so that is accessible in the response filter
   */
  public static void storeCorrelator(
      ContainerRequestContext requestContext,
      Optional<Correlator> correlatorOpt
  ) {
    correlatorOpt.ifPresent(c -> requestContext.setProperty(CONTAINER_PROPERTY_NAME, c));
  }

  public static Optional<Correlator> retrieveCorrelator(ContainerRequestContext requestContext) {
    return Optional.ofNullable(requestContext.getProperty(CONTAINER_PROPERTY_NAME)).map(Correlator.class::cast);
  }

  /**
   * Stores the LocalRequest in the request context so that is accessible in the response filter
   */
  public static void storeRequest(
      ClientRequestContext requestContext,
      LocalRequest localRequest
  ){
    requestContext.setProperty(CLIENT_PROPERTY_NAME, localRequest);
  }

  public static Optional<LocalRequest> retrieveLocalRequest(ClientRequestContext requestContext) {
    return Optional.ofNullable(requestContext.getProperty(CLIENT_PROPERTY_NAME)).map(LocalRequest.class::cast);
  }
}
