package org.zalando.logbook.jaxrs;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.zalando.logbook.Logbook;

import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

public class ClientLoggingFilterTest {

  @Mock
  private ClientRequestContext requestContext;

  @Mock
  private ClientResponseContext responseContext;

  @Mock
  private Logbook logbook;

  private ClientLoggingFilter unit;

  @BeforeEach
  public void setUp() {
    initMocks(this);
    unit = new ClientLoggingFilter(logbook);
  }

  @Test
  public void filterShouldDoNothingIfCorrelatorIsNotPresent() throws Exception {
    unit.filter(requestContext, responseContext);
    verifyZeroInteractions(logbook);
  }
}
