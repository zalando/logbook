package org.zalando.logbook.jaxrs;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.zalando.logbook.Logbook;

import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

public class ServerLoggingFilterTest {

  @Mock
  private ContainerRequestContext requestContext;

  @Mock
  private ContainerResponseContext responseContext;

  @Mock
  private Logbook logbook;

  private ServerLoggingFilter unit;

  @BeforeEach
  public void setUp() {
    initMocks(this);
    unit = new ServerLoggingFilter(logbook);
  }

  @Test
  public void filterShouldDoNothingIfCorrelatorIsNotPresent() throws Exception {
    unit.filter(requestContext, responseContext);
    verifyZeroInteractions(logbook);
  }
}